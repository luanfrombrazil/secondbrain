package io.github.luanfrombrazil.secondbrain.note;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/** Shows the pages of a PDF, with zoom and export. Keeps the old pages until the new ones are ready. */
public final class PdfView extends BorderPane {

    private static final float DPI = 130;
    private static final double MIN_ZOOM = 0.25;
    private static final double MAX_ZOOM = 3;
    private static final double ZOOM_STEP = 1.15;
    private static final double INITIAL_ZOOM = 0.7;

    private final VBox pages = new VBox(10);
    private final ScrollPane scroll = new ScrollPane(pages);
    private final HBox toolbar;
    private final Label zoomLabel = new Label();
    private final AtomicLong latestRequest = new AtomicLong();
    private double zoom = INITIAL_ZOOM;
    private Path current;

    public PdfView() {
        pages.setAlignment(Pos.TOP_CENTER);
        pages.setPadding(new Insets(12));
        scroll.setFitToWidth(true);
        scroll.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                setZoom(zoom * (e.getDeltaY() > 0 ? ZOOM_STEP : 1 / ZOOM_STEP));
                e.consume();
            }
        });

        Button out = new Button("−");
        out.setOnAction(e -> setZoom(zoom / ZOOM_STEP));
        Button in = new Button("+");
        in.setOnAction(e -> setZoom(zoom * ZOOM_STEP));
        Button save = new Button("Salvar PDF");
        save.setOnAction(e -> savePdf());
        zoomLabel.getStyleClass().add("label-text");
        toolbar = new HBox(6, out, zoomLabel, in, save);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(4, 8, 4, 8));
        setTop(toolbar);
        setCenter(scroll);
        updateZoomLabel();
    }

    public void setToolbarVisible(boolean visible) {
        toolbar.setVisible(visible);
        toolbar.setManaged(visible);
    }

    public void show(Path pdf) {
        current = pdf;
        long request = latestRequest.incrementAndGet();
        Thread.ofVirtual().start(() -> {
            try {
                List<WritableImage> images = render(pdf);
                Platform.runLater(() -> {
                    if (request == latestRequest.get()) {
                        double position = scroll.getVvalue();
                        pages.getChildren().setAll(images.stream().map(this::pageView).toList());
                        scroll.setVvalue(position);
                    }
                });
            } catch (IOException e) {
                // keeps showing the last good pages
            }
        });
    }

    private ImageView pageView(WritableImage image) {
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setFitWidth(image.getWidth() * zoom);
        return view;
    }

    private void setZoom(double value) {
        zoom = Math.clamp(value, MIN_ZOOM, MAX_ZOOM);
        pages.getChildren().forEach(node -> {
            ImageView view = (ImageView) node;
            view.setFitWidth(view.getImage().getWidth() * zoom);
        });
        updateZoomLabel();
    }

    private void updateZoomLabel() {
        zoomLabel.setText(String.format(Locale.ROOT, "%.0f%%", zoom * 100));
    }

    /** The compiled PDF only leaves the cache here (D-53). */
    private void savePdf() {
        if (current == null || !Files.exists(current)) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salvar PDF");
        chooser.setInitialFileName(current.getFileName().toString());
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File target = chooser.showSaveDialog(getScene().getWindow());
        if (target != null) {
            try {
                Files.copy(current, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                zoomLabel.setText("Erro ao salvar");
            }
        }
    }

    private static List<WritableImage> render(Path pdf) throws IOException {
        List<WritableImage> images = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(Files.readAllBytes(pdf))) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                images.add(toImage(renderer.renderImageWithDPI(i, DPI, ImageType.RGB)));
            }
        }
        return images;
    }

    private static WritableImage toImage(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = source.getRGB(0, 0, width, height, null, 0, width);
        WritableImage image = new WritableImage(width, height);
        image.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        return image;
    }
}

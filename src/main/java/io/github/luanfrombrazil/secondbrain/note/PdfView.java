package io.github.luanfrombrazil.secondbrain.note;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** Shows the pages of a PDF. Renders in the background and keeps the old pages until the new ones are ready. */
public final class PdfView extends ScrollPane {

    private static final float DPI = 130;

    private final VBox pages = new VBox(10);
    private final AtomicLong latestRequest = new AtomicLong();

    public PdfView() {
        pages.setAlignment(Pos.TOP_CENTER);
        pages.setPadding(new Insets(12));
        setContent(pages);
        setFitToWidth(true);
    }

    public void show(Path pdf) {
        long request = latestRequest.incrementAndGet();
        Thread.ofVirtual().start(() -> {
            try {
                List<WritableImage> images = render(pdf);
                Platform.runLater(() -> {
                    if (request == latestRequest.get()) {
                        double scroll = getVvalue();
                        pages.getChildren().setAll(images.stream().map(ImageView::new).toList());
                        setVvalue(scroll);
                    }
                });
            } catch (IOException e) {
                // keeps showing the last good pages
            }
        });
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

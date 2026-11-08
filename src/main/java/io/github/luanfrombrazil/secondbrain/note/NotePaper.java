package io.github.luanfrombrazil.secondbrain.note;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.nio.file.Path;

/** The note as paper at the right of the graph: a strip peeking, or open beside the graph. */
public final class NotePaper extends BorderPane {

    private static final double PEEK_WIDTH = 130;
    private static final double OPEN_WIDTH = 720;

    private final PdfView pdf = new PdfView();
    private final Button toggle = new Button("<");
    private final Button edit = new Button("Editar");
    private boolean open;

    /** @param pdfFile the last compiled PDF */
    public NotePaper(Path pdfFile, Runnable onEdit) {
        edit.setOnAction(e -> onEdit.run());
        toggle.setOnAction(e -> setOpen(!open));
        HBox bar = new HBox(6, toggle, edit);
        bar.setPadding(new Insets(6));
        bar.getStyleClass().add("panel");
        setTop(bar);
        pdf.show(pdfFile);
        setCenter(pdf);
        setOpen(false);
    }

    private void setOpen(boolean value) {
        open = value;
        toggle.setText(open ? ">" : "<");
        edit.setVisible(open);
        edit.setManaged(open);
        pdf.setToolbarVisible(open);
        double width = open ? OPEN_WIDTH : PEEK_WIDTH;
        setMinWidth(width);
        setPrefWidth(width);
        setMaxWidth(width);
    }
}

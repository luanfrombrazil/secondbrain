package io.github.luanfrombrazil.secondbrain.graph.view.backdrop.lined;

import io.github.luanfrombrazil.secondbrain.graph.view.backdrop.Backdrop;
import io.github.luanfrombrazil.secondbrain.graph.view.backdrop.common.TwoLevelGrid;
import io.github.luanfrombrazil.secondbrain.graph.view.camera.Camera;
import javafx.scene.canvas.GraphicsContext;

/** Horizontal lines, like ruled paper. */
public final class LinedBackdrop implements Backdrop {

    @Override
    public String name() {
        return "Pautado";
    }

    @Override
    public void draw(GraphicsContext g, Camera camera, double width, double height) {
        TwoLevelGrid.draw(g, camera, (step, x0, y0) -> {
            for (double y = y0; y < height; y += step) {
                double py = Math.floor(y) + 0.5;
                g.strokeLine(0, py, width, py);
            }
        });
    }
}

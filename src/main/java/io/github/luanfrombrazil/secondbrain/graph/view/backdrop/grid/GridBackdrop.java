package io.github.luanfrombrazil.secondbrain.graph.view.backdrop.grid;

import io.github.luanfrombrazil.secondbrain.graph.view.backdrop.Backdrop;
import io.github.luanfrombrazil.secondbrain.graph.view.backdrop.common.TwoLevelGrid;
import io.github.luanfrombrazil.secondbrain.graph.view.camera.Camera;
import javafx.scene.canvas.GraphicsContext;

/** Squares, like graph paper. */
public final class GridBackdrop implements Backdrop {

    @Override
    public String name() {
        return "Quadriculado";
    }

    @Override
    public void draw(GraphicsContext g, Camera camera, double width, double height) {
        TwoLevelGrid.draw(g, camera, (step, x0, y0) -> {
            for (double y = y0; y < height; y += step) {
                double py = Math.floor(y) + 0.5;
                g.strokeLine(0, py, width, py);
            }
            for (double x = x0; x < width; x += step) {
                double px = Math.floor(x) + 0.5;
                g.strokeLine(px, 0, px, height);
            }
        });
    }
}

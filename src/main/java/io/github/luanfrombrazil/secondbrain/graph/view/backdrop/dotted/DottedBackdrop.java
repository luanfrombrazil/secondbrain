package io.github.luanfrombrazil.secondbrain.graph.view.backdrop.dotted;

import io.github.luanfrombrazil.secondbrain.graph.view.backdrop.Backdrop;
import io.github.luanfrombrazil.secondbrain.graph.view.backdrop.common.TwoLevelGrid;
import io.github.luanfrombrazil.secondbrain.graph.view.camera.Camera;
import javafx.scene.canvas.GraphicsContext;

/** A square dot at each grid crossing. */
public final class DottedBackdrop implements Backdrop {

    @Override
    public String name() {
        return "Pontilhado";
    }

    @Override
    public void draw(GraphicsContext g, Camera camera, double width, double height) {
        double half = TwoLevelGrid.LINE_WIDTH;
        TwoLevelGrid.draw(g, camera, (step, x0, y0) -> {
            for (double x = x0; x < width; x += step) {
                for (double y = y0; y < height; y += step) {
                    g.fillRect(x - half, y - half, 2 * half, 2 * half);
                }
            }
        });
    }
}

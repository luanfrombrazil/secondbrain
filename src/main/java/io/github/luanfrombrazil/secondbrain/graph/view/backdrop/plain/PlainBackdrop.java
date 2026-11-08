package io.github.luanfrombrazil.secondbrain.graph.view.backdrop.plain;

import io.github.luanfrombrazil.secondbrain.graph.view.backdrop.Backdrop;
import io.github.luanfrombrazil.secondbrain.graph.view.camera.Camera;
import javafx.scene.canvas.GraphicsContext;

/** No pattern at all. */
public final class PlainBackdrop implements Backdrop {

    @Override
    public String name() {
        return "Liso";
    }

    @Override
    public void draw(GraphicsContext g, Camera camera, double width, double height) {
        // nothing to draw
    }
}

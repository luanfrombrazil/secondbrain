package io.github.luanfrombrazil.secondbrain.graph.view.backdrop;

import io.github.luanfrombrazil.secondbrain.graph.view.camera.Camera;
import javafx.scene.canvas.GraphicsContext;

/**
 * A pattern for the background of the space. Each one lives in its own package.
 * To add one, implement this interface in a new package and list it in {@link Backdrops}.
 */
public interface Backdrop {

    /** The name shown to the user. */
    String name();

    void draw(GraphicsContext g, Camera camera, double width, double height);
}

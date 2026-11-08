package io.github.luanfrombrazil.secondbrain.graph.view.backdrop.common;

import io.github.luanfrombrazil.secondbrain.graph.view.camera.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** A helper for regular patterns with two levels: zooming in fades in a level 5 times smaller. */
public final class TwoLevelGrid {

    public static final double LINE_WIDTH = 1.2;

    private static final double WORLD_STEP = 230;
    private static final double MIN_SCREEN_STEP = 15;
    private static final double OPACITY = 0.2;
    private static final Color COLOR = Color.web("#8d93a3");

    /** Draws one level of the pattern. */
    @FunctionalInterface
    public interface Level {
        /** @param x0 @param y0 the first grid position on screen, left of and above the view */
        void mark(double step, double x0, double y0);
    }

    private TwoLevelGrid() {
    }

    public static void draw(GraphicsContext g, Camera c, Level level) {
        double minorStep = WORLD_STEP * c.scale();
        while (minorStep < MIN_SCREEN_STEP) {
            minorStep *= 5;
        }
        while (minorStep >= MIN_SCREEN_STEP * 5) {
            minorStep /= 5;
        }
        double t = Math.log(minorStep / MIN_SCREEN_STEP) / Math.log(5);
        double ramp = Math.min(1, t * 2);
        double fadeIn = ramp * ramp * (3 - 2 * ramp);

        g.setStroke(COLOR);
        g.setFill(COLOR);
        g.setLineWidth(LINE_WIDTH);
        g.setGlobalAlpha(OPACITY * fadeIn);
        markLevel(c, minorStep, level);
        g.setGlobalAlpha(OPACITY);
        markLevel(c, minorStep * 5, level);
        g.setGlobalAlpha(1);
    }

    private static void markLevel(Camera c, double step, Level level) {
        double originX = c.screenX(0);
        double originY = c.screenY(0);
        level.mark(step, originX - Math.ceil(originX / step) * step, originY - Math.ceil(originY / step) * step);
    }
}

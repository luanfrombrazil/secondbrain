package io.github.luanfrombrazil.secondbrain.graph.view;

import io.github.luanfrombrazil.secondbrain.graph.Parameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** The background of the space. The grid has two levels: zooming in fades in squares 5 times smaller. */
public enum Backdrop {
    PLAIN("Liso"), LINED("Pautado"), DOTTED("Pontilhado"), GRID("Quadriculado");

    private static final Color COLOR = Color.web("#8d93a3");

    private final String name;

    Backdrop(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    void draw(GraphicsContext g, Camera c, double width, double height, Parameters p) {
        if (this == PLAIN) {
            return;
        }
        double minScreenStep = p.backdropMinScreenStep;
        double minorStep = p.backdropStep * c.scale();
        while (minorStep < minScreenStep) {
            minorStep *= 5;
        }
        while (minorStep >= minScreenStep * 5) {
            minorStep /= 5;
        }
        double t = Math.log(minorStep / minScreenStep) / Math.log(5);
        double ramp = Math.min(1, t * 2);
        double fadeIn = ramp * ramp * (3 - 2 * ramp);
        double majorStep = minorStep * 5;

        double originX = c.screenX(0);
        double originY = c.screenY(0);
        g.setStroke(COLOR);
        g.setFill(COLOR);
        g.setLineWidth(p.backdropLineWidth);
        g.setGlobalAlpha(p.backdropOpacity * fadeIn);
        mark(g, minorStep, originX, originY, width, height, p.backdropLineWidth);
        g.setGlobalAlpha(p.backdropOpacity);
        mark(g, majorStep, originX, originY, width, height, p.backdropLineWidth);
        g.setGlobalAlpha(1);
    }

    private void mark(GraphicsContext g, double step, double ox, double oy, double width, double height, double thickness) {
        double x0 = ox - Math.ceil(ox / step) * step;
        double y0 = oy - Math.ceil(oy / step) * step;
        switch (this) {
            case LINED -> horizontalLines(g, step, y0, width, height);
            case GRID -> {
                horizontalLines(g, step, y0, width, height);
                for (double x = x0; x < width; x += step) {
                    double px = Math.floor(x) + 0.5;
                    g.strokeLine(px, 0, px, height);
                }
            }
            case DOTTED -> {
                for (double x = x0; x < width; x += step) {
                    for (double y = y0; y < height; y += step) {
                        g.fillRect(x - thickness, y - thickness, 2 * thickness, 2 * thickness);
                    }
                }
            }
            case PLAIN -> {
            }
        }
    }

    private void horizontalLines(GraphicsContext g, double step, double y0, double width, double height) {
        for (double y = y0; y < height; y += step) {
            double py = Math.floor(y) + 0.5;
            g.strokeLine(0, py, width, py);
        }
    }
}

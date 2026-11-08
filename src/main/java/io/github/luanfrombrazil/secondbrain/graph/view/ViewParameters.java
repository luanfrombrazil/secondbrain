package io.github.luanfrombrazil.secondbrain.graph.view;

import java.util.Locale;

/** The tunable numbers of how the graph is drawn. The values here are the defaults. */
public final class ViewParameters {

    public double arrowWidth = 2.2;

    /** The scale at which each zoom level turns into the next, from farthest to nearest. */
    public final double[] zoomThresholds = {0.14, 0.55, 0.86};

    public String asText() {
        return String.format(Locale.ROOT, "arrowWidth = %.1f%nzoomThresholds = %.2f, %.2f, %.2f%n",
                arrowWidth, zoomThresholds[0], zoomThresholds[1], zoomThresholds[2]);
    }
}

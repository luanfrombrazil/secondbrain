package io.github.luanfrombrazil.secondbrain.graph;

import java.util.Locale;

/** The tunable numbers of the layouts and the zoom. The values here are the defaults. */
public final class Parameters {

    // By dependency
    public double columnGap = 130;
    public double rowGap = 40;

    // Radial
    public double ringStep = 400;

    // By forces
    public double repulsion = 14000;
    public double springStrength = 0.12;
    public double springLength = 340;
    public double gravity = 0.015;
    public double friction = 0.6;
    public double folderForce = 0.03;


    // Arrows
    public double arrowWidth = 2.2;

    // Zoom thresholds between the five levels, from farthest to nearest
    public final double[] zoomThresholds = {0.14, 0.30, 0.55, 0.86};

    public String asText() {
        return String.format(Locale.ROOT, """
                columnGap = %.0f
                rowGap = %.0f
                ringStep = %.0f
                repulsion = %.0f
                springStrength = %.3f
                springLength = %.0f
                gravity = %.3f
                friction = %.2f
                folderForce = %.3f
                arrowWidth = %.1f
                zoomThresholds = %.2f, %.2f, %.2f, %.2f
                """, columnGap, rowGap, ringStep, repulsion, springStrength, springLength,
                gravity, friction, folderForce, arrowWidth,
                zoomThresholds[0], zoomThresholds[1], zoomThresholds[2], zoomThresholds[3]);
    }
}

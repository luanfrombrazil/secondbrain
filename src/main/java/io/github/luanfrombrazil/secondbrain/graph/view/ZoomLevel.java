package io.github.luanfrombrazil.secondbrain.graph.view;

/** The semantic zoom levels, from farthest to nearest. */
public enum ZoomLevel {
    FARTHEST, VERY_FAR, FAR, MEDIUM, NEAR;

    /** Margin around each threshold, so the card does not flicker when the zoom rests on it. */
    private static final double HYSTERESIS = 0.05;

    /** @param thresholds the scale at which each level turns into the next */
    public static ZoomLevel forScale(double scale, ZoomLevel current, double[] thresholds) {
        int level = 0;
        while (level < thresholds.length) {
            double margin = current.ordinal() > level ? 1 - HYSTERESIS : 1 + HYSTERESIS;
            if (scale < thresholds[level] * margin) {
                break;
            }
            level++;
        }
        return values()[level];
    }
}

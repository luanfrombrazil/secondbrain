package io.github.luanfrombrazil.secondbrain.graph.layout;

import io.github.luanfrombrazil.secondbrain.graph.Point;

import java.util.Map;

/** One animated arrangement in progress. */
public interface LayoutRun {

    /** Advances one frame. Returns false once it has settled. */
    boolean step();

    Map<String, Point> positions();
}

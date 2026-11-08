package io.github.luanfrombrazil.secondbrain.graph;

import java.util.Map;

/** A way to arrange the graph: returns the center of each node. */
public interface Layout {

    Map<String, Point> compute(Graph graph, Parameters parameters);
}

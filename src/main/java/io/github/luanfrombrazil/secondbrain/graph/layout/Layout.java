package io.github.luanfrombrazil.secondbrain.graph.layout;

import io.github.luanfrombrazil.secondbrain.graph.Graph;
import io.github.luanfrombrazil.secondbrain.graph.Point;

import java.util.List;
import java.util.Map;

/**
 * A way to arrange the graph. Each layout lives in its own package and keeps its own settings.
 * To add one, implement this interface in a new package and list it in {@link Layouts}.
 */
public interface Layout {

    /** The name shown to the user. */
    String name();

    /** The center of each node. */
    Map<String, Point> compute(Graph graph);

    /** The settings the user can change. Controls are built from this list. */
    default List<Tunable> tunables() {
        return List.of();
    }

    /** Whether connections are drawn as left-to-right curves instead of straight lines. */
    default boolean curvesConnections() {
        return false;
    }
}

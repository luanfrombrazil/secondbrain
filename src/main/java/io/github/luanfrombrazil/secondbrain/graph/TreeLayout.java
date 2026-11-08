package io.github.luanfrombrazil.secondbrain.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * What tree layouts share: connected cards are arranged by the subclass,
 * a loose card with a fixed point stays there and pushes its neighbours, and other loose cards go to a strip.
 */
abstract class TreeLayout implements Layout {

    private static final int PER_STRIP_ROW = 8;

    protected abstract Map<String, Point> arrange(Graph graph, Forest forest, Parameters parameters);

    @Override
    public final Map<String, Point> compute(Graph graph, Parameters p) {
        Map<String, Point> positions = new HashMap<>(arrange(graph, new Forest(graph), p));
        placeInStrip(graph, positions, p);

        Set<String> fixedIds = new HashSet<>();
        for (Node node : graph.nodes()) {
            if (graph.loose(node.id()) && node.fixed() != null) {
                positions.put(node.id(), node.fixed());
                fixedIds.add(node.id());
            }
        }
        return MagneticRepulsion.apply(positions, fixedIds, p.magneticRadius);
    }

    private void placeInStrip(Graph graph, Map<String, Point> positions, Parameters p) {
        double left = positions.values().stream().mapToDouble(Point::x).min().orElse(0);
        double bottom = positions.values().stream().mapToDouble(Point::y).max().orElse(0);
        double stepX = Node.WIDTH + 30;
        double stepY = Node.HEIGHT + p.rowGap;
        double stripTop = positions.isEmpty() ? 0 : bottom + 1.6 * stepY;
        int i = 0;
        for (Node node : graph.nodes()) {
            if (graph.loose(node.id()) && node.fixed() == null) {
                positions.put(node.id(), new Point(
                        left + (i % PER_STRIP_ROW) * stepX,
                        stripTop + (i / PER_STRIP_ROW) * stepY));
                i++;
            }
        }
    }
}

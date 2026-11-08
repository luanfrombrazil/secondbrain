package io.github.luanfrombrazil.secondbrain.graph.layout.common;

import io.github.luanfrombrazil.secondbrain.graph.Graph;
import io.github.luanfrombrazil.secondbrain.graph.Node;
import io.github.luanfrombrazil.secondbrain.graph.Point;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Where cards without connections go: one with a fixed point stays there and pushes its
 * neighbours away, and the others line up in a strip below the arranged cards.
 */
public final class LooseCards {

    private static final int PER_STRIP_ROW = 8;
    private static final double MAGNETIC_RADIUS = 200;

    private LooseCards() {
    }

    /** @param arranged the positions of the connected cards */
    public static Map<String, Point> place(Graph graph, Map<String, Point> arranged, double rowGap) {
        Map<String, Point> positions = new HashMap<>(arranged);
        placeInStrip(graph, positions, rowGap);

        Set<String> fixedIds = new HashSet<>();
        for (Node node : graph.nodes()) {
            if (graph.loose(node.id()) && node.fixed() != null) {
                positions.put(node.id(), node.fixed());
                fixedIds.add(node.id());
            }
        }
        return MagneticRepulsion.apply(positions, fixedIds, MAGNETIC_RADIUS);
    }

    private static void placeInStrip(Graph graph, Map<String, Point> positions, double rowGap) {
        double left = positions.values().stream().mapToDouble(Point::x).min().orElse(0);
        double bottom = positions.values().stream().mapToDouble(Point::y).max().orElse(0);
        double stepX = Node.WIDTH + 30;
        double stepY = Node.HEIGHT + rowGap;
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

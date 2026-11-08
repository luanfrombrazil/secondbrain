package io.github.luanfrombrazil.secondbrain.graph;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Columns by depth: each card sits to the right of everything it depends on.
 * A parent is centered between its children; with a single child, both share a row.
 */
public final class DependencyLayout extends TreeLayout {

    private static final int ORDERING_PASSES = 3;

    @Override
    protected Map<String, Point> arrange(Graph graph, Forest f, Parameters p) {
        double stepX = Node.WIDTH + p.columnGap;
        double stepY = Node.HEIGHT + p.rowGap;

        Map<String, Double> ys = new HashMap<>();
        for (int pass = 0; ; pass++) {
            distribute(f, stepY, ys);
            if (pass == ORDERING_PASSES) {
                break;
            }
            reorder(graph, f, ys);
        }

        Map<String, Point> positions = new HashMap<>();
        ys.forEach((id, y) -> positions.put(id, new Point(graph.layer(id) * stepX, y)));
        return positions;
    }

    private void distribute(Forest f, double stepY, Map<String, Double> ys) {
        ys.clear();
        double[] cursor = {0};
        for (String root : f.roots) {
            place(root, f, stepY, ys, cursor);
            cursor[0] += stepY * 0.5;
        }
    }

    private double place(String id, Forest f, double stepY, Map<String, Double> ys, double[] cursor) {
        List<String> children = f.children.get(id);
        double y;
        if (children.isEmpty()) {
            y = cursor[0];
            cursor[0] += stepY;
        } else {
            double first = place(children.getFirst(), f, stepY, ys, cursor);
            double last = first;
            for (String child : children.subList(1, children.size())) {
                last = place(child, f, stepY, ys, cursor);
            }
            y = (first + last) / 2;
        }
        ys.put(id, y);
        return y;
    }

    /** Moves each card toward the mean height of its prerequisites, so fewer arrows cross. */
    private void reorder(Graph graph, Forest f, Map<String, Double> ys) {
        Comparator<String> order = Comparator
                .comparing((String id) -> graph.node(id).folder(), Forest.FOLDERLESS_LAST)
                .thenComparingDouble(id -> graph.dependenciesOf(id).stream()
                        .mapToDouble(ys::get).average().orElse(ys.get(id)))
                .thenComparing(Comparator.naturalOrder());
        f.roots.sort(order);
        f.children.values().forEach(list -> list.sort(order));
    }
}

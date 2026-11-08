package io.github.luanfrombrazil.secondbrain.graph.layout.radial;

import io.github.luanfrombrazil.secondbrain.graph.Edge;
import io.github.luanfrombrazil.secondbrain.graph.Graph;
import io.github.luanfrombrazil.secondbrain.graph.Node;
import io.github.luanfrombrazil.secondbrain.graph.Point;
import io.github.luanfrombrazil.secondbrain.graph.layout.Layout;
import io.github.luanfrombrazil.secondbrain.graph.layout.Tunable;
import io.github.luanfrombrazil.secondbrain.graph.layout.common.Forest;
import io.github.luanfrombrazil.secondbrain.graph.layout.common.LooseCards;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Rings by dependency level. Each subtree gets a slice proportional to its leaves.
 * With several roots, they form the first ring around an empty center.
 */
public final class RadialLayout implements Layout {

    private static final double LOOSE_ROW_GAP = 40;

    private double ringStep = 400;

    private static final double SEPARATION = Math.hypot(Node.WIDTH, Node.HEIGHT) + 6;

    @Override
    public String name() {
        return "Radial";
    }

    @Override
    public List<Tunable> tunables() {
        return List.of(new Tunable("Distância entre anéis", 260, 900, () -> ringStep, v -> ringStep = v));
    }

    @Override
    public Map<String, Point> compute(Graph graph) {
        return LooseCards.place(graph, arrange(graph, new Forest(graph)), LOOSE_ROW_GAP);
    }

    private Map<String, Point> arrange(Graph graph, Forest f) {
        int offset = f.roots.size() == 1 ? 0 : 1;
        TreeMap<Integer, List<String>> rings = new TreeMap<>();
        f.children.keySet().forEach(id ->
                rings.computeIfAbsent(graph.layer(id) + offset, k -> new ArrayList<>()).add(id));

        double step = ringStep;
        for (var ring : rings.entrySet()) {
            if (ring.getKey() > 0) {
                step = Math.max(step, ring.getValue().size() * SEPARATION / (2 * Math.PI * ring.getKey()));
            }
        }

        Map<String, Double> angles = new HashMap<>();
        double total = f.roots.stream().mapToInt(f::leaves).sum();
        double cursor = 0;
        for (String root : f.roots) {
            double end = cursor + 2 * Math.PI * f.leaves(root) / total;
            slice(root, cursor, end, f, angles);
            cursor = end;
        }

        // A node with more than one parent sits at the angle between them.
        for (var ring : rings.entrySet()) {
            for (String id : ring.getValue()) {
                if (graph.dependenciesOf(id).size() > 1) {
                    angles.put(id, circularMean(graph.dependenciesOf(id), angles));
                }
            }
        }

        Map<String, Point> positions = new HashMap<>();
        for (var ring : rings.entrySet()) {
            double radius = ring.getKey() * step;
            if (radius > 0) {
                spread(ring.getValue(), angles, SEPARATION / radius);
            }
            for (String id : ring.getValue()) {
                double angle = angles.get(id);
                positions.put(id, new Point(radius * Math.cos(angle), radius * Math.sin(angle)));
            }
        }
        return positions;
    }

    private void slice(String id, double start, double end, Forest f, Map<String, Double> angles) {
        angles.put(id, (start + end) / 2);
        double cursor = start;
        for (String child : f.children.get(id)) {
            double share = (end - start) * f.leaves(child) / f.leaves(id);
            slice(child, cursor, cursor + share, f, angles);
            cursor += share;
        }
    }

    private double circularMean(List<String> ids, Map<String, Double> angles) {
        double x = ids.stream().mapToDouble(id -> Math.cos(angles.get(id))).sum();
        double y = ids.stream().mapToDouble(id -> Math.sin(angles.get(id))).sum();
        return Math.atan2(y, x);
    }

    /** Enforces the minimum distance between ring neighbours. The ring step already guarantees they fit. */
    private void spread(List<String> ring, Map<String, Double> angles, double minimum) {
        ring.sort(Comparator.comparingDouble(angles::get));
        for (int i = 1; i < ring.size(); i++) {
            angles.merge(ring.get(i), angles.get(ring.get(i - 1)) + minimum, Math::max);
        }
        double limit = angles.get(ring.getFirst()) + 2 * Math.PI - minimum;
        for (int i = ring.size() - 1; i > 0; i--) {
            angles.merge(ring.get(i), limit, Math::min);
            limit = angles.get(ring.get(i)) - minimum;
        }
    }
}

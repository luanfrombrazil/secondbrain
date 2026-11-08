package io.github.luanfrombrazil.secondbrain.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The connected nodes, organized as trees. Each node has a main parent, chosen from the layer
 * right before it. Other dependencies remain connections but do not decide the position.
 */
final class Forest {

    final List<String> roots = new ArrayList<>();
    final Map<String, List<String>> children = new HashMap<>();
    private final Map<String, Integer> leaves = new HashMap<>();

    Forest(Graph graph) {
        Comparator<String> order = Comparator.naturalOrder();
        for (Node node : graph.nodes()) {
            if (graph.loose(node.id())) {
                continue;
            }
            children.putIfAbsent(node.id(), new ArrayList<>());
            int layer = graph.layer(node.id());
            if (layer == 0) {
                roots.add(node.id());
                continue;
            }
            String parent = graph.dependenciesOf(node.id()).stream()
                    .filter(d -> graph.layer(d) == layer - 1)
                    .findFirst().orElseThrow();
            children.computeIfAbsent(parent, p -> new ArrayList<>()).add(node.id());
        }
        roots.sort(order);
        children.values().forEach(list -> list.sort(order));
        roots.forEach(this::countLeaves);
    }

    private int countLeaves(String id) {
        int total = children.get(id).stream().mapToInt(this::countLeaves).sum();
        int result = Math.max(1, total);
        leaves.put(id, result);
        return result;
    }

    int leaves(String id) {
        return leaves.get(id);
    }
}

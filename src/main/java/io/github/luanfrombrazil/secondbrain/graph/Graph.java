package io.github.luanfrombrazil.secondbrain.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The input of the layouts: nodes, connections (dependencies, acyclic) and links.
 * It is immutable and knows nothing about screens or files.
 */
public final class Graph {

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final List<Edge> connections;
    private final List<Edge> links;
    private final Map<String, List<String>> dependencies = new HashMap<>();
    private final Map<String, List<String>> dependents = new HashMap<>();
    private final Map<String, Integer> layers = new HashMap<>();

    public Graph(Collection<Node> nodes, List<Edge> connections, List<Edge> links) {
        nodes.forEach(node -> this.nodes.put(node.id(), node));
        this.connections = List.copyOf(connections);
        this.links = List.copyOf(links);
        for (Node node : nodes) {
            dependencies.put(node.id(), new ArrayList<>());
            dependents.put(node.id(), new ArrayList<>());
        }
        for (Edge a : connections) {
            require(a.source());
            require(a.target());
            dependencies.get(a.target()).add(a.source());
            dependents.get(a.source()).add(a.target());
        }
        links.forEach(a -> {
            require(a.source());
            require(a.target());
        });
        computeLayers();
    }

    private void require(String id) {
        if (!nodes.containsKey(id)) {
            throw new IllegalArgumentException("Unknown node: " + id);
        }
    }

    /** Longest path from the roots (Kahn): each node comes after everything it depends on. */
    private void computeLayers() {
        Map<String, Integer> remaining = new HashMap<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        for (String id : nodes.keySet()) {
            remaining.put(id, dependencies.get(id).size());
            layers.put(id, 0);
            if (dependencies.get(id).isEmpty()) {
                queue.add(id);
            }
        }
        int seen = 0;
        while (!queue.isEmpty()) {
            String id = queue.poll();
            seen++;
            for (String child : dependents.get(id)) {
                layers.merge(child, layers.get(id) + 1, Math::max);
                if (remaining.merge(child, -1, Integer::sum) == 0) {
                    queue.add(child);
                }
            }
        }
        if (seen != nodes.size()) {
            throw new IllegalArgumentException("The connections form a cycle");
        }
    }

    public Collection<Node> nodes() {
        return nodes.values();
    }

    public Node node(String id) {
        return nodes.get(id);
    }

    public List<Edge> connections() {
        return connections;
    }

    public List<Edge> links() {
        return links;
    }

    public List<String> dependenciesOf(String id) {
        return dependencies.get(id);
    }

    public List<String> dependentsOf(String id) {
        return dependents.get(id);
    }

    public int layer(String id) {
        return layers.get(id);
    }

    /** A loose node has no connections. Links do not count. */
    public boolean loose(String id) {
        return dependencies.get(id).isEmpty() && dependents.get(id).isEmpty();
    }

    /** The same graph with loose cards released from where they were created. */
    public Graph withoutFixedPoints() {
        return new Graph(nodes.values().stream().map(Node::withoutFixed).toList(), connections, links);
    }

    /** False for a card onto itself, a repeated connection, or one that would close a cycle. */
    public boolean canConnect(String source, String target) {
        return !source.equals(target)
                && !dependencies.get(target).contains(source)
                && !reaches(target, source);
    }

    private boolean reaches(String from, String to) {
        Set<String> seen = new HashSet<>();
        ArrayDeque<String> pending = new ArrayDeque<>(List.of(from));
        while (!pending.isEmpty()) {
            String id = pending.pop();
            if (id.equals(to)) {
                return true;
            }
            if (seen.add(id)) {
                pending.addAll(dependents.get(id));
            }
        }
        return false;
    }

    /** Adds a connection. Its two cards lose their fixed points and follow the layout from now on. */
    public Graph withConnection(String source, String target) {
        if (!canConnect(source, target)) {
            throw new IllegalArgumentException("Invalid connection: " + source + " -> " + target);
        }
        List<Node> updated = nodes.values().stream()
                .map(n -> n.id().equals(source) || n.id().equals(target) ? n.withoutFixed() : n)
                .toList();
        List<Edge> edges = new ArrayList<>(connections);
        edges.add(new Edge(source, target));
        return new Graph(updated, edges, links);
    }

    public Graph withNode(Node node) {
        List<Node> updated = new ArrayList<>(nodes.values());
        updated.add(node);
        return new Graph(updated, connections, links);
    }
}

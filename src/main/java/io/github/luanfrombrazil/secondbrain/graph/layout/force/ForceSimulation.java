package io.github.luanfrombrazil.secondbrain.graph.layout.force;

import io.github.luanfrombrazil.secondbrain.graph.Edge;
import io.github.luanfrombrazil.secondbrain.graph.Graph;
import io.github.luanfrombrazil.secondbrain.graph.Node;
import io.github.luanfrombrazil.secondbrain.graph.Point;
import io.github.luanfrombrazil.secondbrain.graph.layout.LayoutRun;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Cards repel each other (Barnes-Hut), connections and links pull like springs, and the system cools until it stops.
 * The same graph and parameters always give the same result.
 */
final class ForceSimulation implements LayoutRun {

    private static final double THETA = 0.8;
    private static final double COOLING = 0.08;
    private static final double MIN_ALPHA = 0.002;
    private static final double LINK_STRENGTH = 0.35;
    private static final double FIXED_MASS = 3;
    private static final double GAP = 16;

    private final ForceSettings p;
    private final String[] ids;
    private final double[] x;
    private final double[] y;
    private final double[] vx;
    private final double[] vy;
    private final boolean[] pinned;
    private final double[] mass;
    private final int[][] connections;
    private final int[][] links;
    private final Map<String, List<Integer>> folders = new HashMap<>();
    private double alpha = 1;

    ForceSimulation(Graph graph, ForceSettings p, Map<String, Point> start) {
        this.p = p;
        ids = graph.nodes().stream().map(Node::id).sorted().toArray(String[]::new);
        int n = ids.length;
        x = new double[n];
        y = new double[n];
        vx = new double[n];
        vy = new double[n];
        pinned = new boolean[n];
        mass = new double[n];
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < n; i++) {
            index.put(ids[i], i);
            Node node = graph.node(ids[i]);
            Point point = node.fixed() != null && graph.loose(node.id()) ? node.fixed() : start.get(ids[i]);
            pinned[i] = node.fixed() != null && graph.loose(node.id());
            mass[i] = pinned[i] ? FIXED_MASS : 1;
            if (point == null) {
                // Sunflower spiral: spreads points without randomness.
                double radius = 300 * Math.sqrt(i + 0.5);
                double angle = i * Math.PI * (3 - Math.sqrt(5));
                point = new Point(radius * Math.cos(angle), radius * Math.sin(angle));
            }
            x[i] = point.x();
            y[i] = point.y();
            if (node.folder() != null) {
                folders.computeIfAbsent(node.folder(), k -> new ArrayList<>()).add(i);
            }
        }
        connections = pairs(graph.connections(), index);
        links = pairs(graph.links(), index);
    }

    private static int[][] pairs(List<Edge> edges, Map<String, Integer> index) {
        return edges.stream()
                .map(a -> new int[]{index.get(a.source()), index.get(a.target())})
                .toArray(int[][]::new);
    }

    /** Advances one frame. Returns false once the system has settled. */
    @Override
    public boolean step() {
        if (alpha < MIN_ALPHA) {
            return false;
        }
        repel();
        pull(connections, p.springStrength);
        pull(links, p.springStrength * LINK_STRENGTH);
        pullFolders();
        for (int i = 0; i < ids.length; i++) {
            if (pinned[i]) {
                vx[i] = 0;
                vy[i] = 0;
                continue;
            }
            vx[i] = (vx[i] - x[i] * p.gravity * alpha) * p.friction;
            vy[i] = (vy[i] - y[i] * p.gravity * alpha) * p.friction;
            x[i] += vx[i];
            y[i] += vy[i];
        }
        separateTouching();
        alpha *= 1 - COOLING;
        return true;
    }

    public void reheat() {
        alpha = Math.max(alpha, 0.6);
    }

    @Override
    public Map<String, Point> positions() {
        Map<String, Point> output = new HashMap<>();
        for (int i = 0; i < ids.length; i++) {
            output.put(ids[i], new Point(x[i], y[i]));
        }
        return output;
    }

    private void repel() {
        int n = ids.length;
        if (n < 2) {
            return;
        }
        double minX = Arrays.stream(x).min().orElse(0);
        double maxX = Arrays.stream(x).max().orElse(0);
        double minY = Arrays.stream(y).min().orElse(0);
        double maxY = Arrays.stream(y).max().orElse(0);
        double half = Math.max(maxX - minX, maxY - minY) / 2 + 1;
        Cell root = new Cell((minX + maxX) / 2, (minY + maxY) / 2, half);
        for (int i = 0; i < n; i++) {
            root.insert(i, x, y, mass, 0);
        }
        for (int i = 0; i < n; i++) {
            if (!pinned[i]) {
                root.apply(i, x, y, vx, vy, p.repulsion * alpha);
            }
        }
    }

    private void pull(int[][] pairs, double strength) {
        for (int[] pair : pairs) {
            int a = pair[0];
            int b = pair[1];
            double dx = x[b] - x[a];
            double dy = y[b] - y[a];
            double d = Math.max(1, Math.hypot(dx, dy));
            double k = (d - p.springLength) / d * strength * alpha;
            vx[a] += dx * k;
            vy[a] += dy * k;
            vx[b] -= dx * k;
            vy[b] -= dy * k;
        }
    }

    private void pullFolders() {
        for (List<Integer> folder : folders.values()) {
            double cx = folder.stream().mapToDouble(i -> x[i]).average().orElse(0);
            double cy = folder.stream().mapToDouble(i -> y[i]).average().orElse(0);
            for (int i : folder) {
                vx[i] += (cx - x[i]) * p.folderForce * alpha;
                vy[i] += (cy - y[i]) * p.folderForce * alpha;
            }
        }
    }

    /** Sweep along x: only compares cards that may be touching. */
    private void separateTouching() {
        double minX = Node.WIDTH + GAP;
        double minY = Node.HEIGHT + GAP;
        for (int pass = 0; pass < 3; pass++) {
            Integer[] order = IntStream.range(0, ids.length).boxed().toArray(Integer[]::new);
            Arrays.sort(order, Comparator.comparingDouble(i -> x[i]));
            for (int ia = 0; ia < order.length; ia++) {
                int a = order[ia];
                for (int ib = ia + 1; ib < order.length && x[order[ib]] - x[a] < minX; ib++) {
                    separate(a, order[ib], minX, minY);
                }
            }
        }
    }

    private void separate(int a, int b, double minX, double minY) {
        double dx = x[b] - x[a];
        double dy = y[b] - y[a];
        double overlapX = minX - Math.abs(dx);
        double overlapY = minY - Math.abs(dy);
        if (overlapX <= 0 || overlapY <= 0) {
            return;
        }
        double shareA = pinned[a] ? 0 : pinned[b] ? 1 : 0.5;
        double shareB = pinned[b] ? 0 : pinned[a] ? 1 : 0.5;
        if (overlapY < overlapX) {
            double sign = dy >= 0 ? 1 : -1;
            y[a] -= sign * overlapY * shareA;
            y[b] += sign * overlapY * shareB;
        } else {
            double sign = dx >= 0 ? 1 : -1;
            x[a] -= sign * overlapX * shareA;
            x[b] += sign * overlapX * shareB;
        }
    }

    /** Quadtree: from afar, a group of cards acts as a single point. */
    private static final class Cell {
        private final double cx;
        private final double cy;
        private final double half;
        private double totalMass;
        private double mx;
        private double my;
        private int single = -1;
        private Cell[] children;

        Cell(double cx, double cy, double half) {
            this.cx = cx;
            this.cy = cy;
            this.half = half;
        }

        void insert(int i, double[] x, double[] y, double[] mass, int depth) {
            double updated = totalMass + mass[i];
            mx = (mx * totalMass + x[i] * mass[i]) / updated;
            my = (my * totalMass + y[i] * mass[i]) / updated;
            boolean empty = totalMass == 0;
            totalMass = updated;
            if (empty) {
                single = i;
                return;
            }
            if (depth >= 32) {
                return;
            }
            if (children == null) {
                children = new Cell[4];
                int previous = single;
                single = -1;
                child(previous, x, y).insert(previous, x, y, mass, depth + 1);
            }
            child(i, x, y).insert(i, x, y, mass, depth + 1);
        }

        private Cell child(int i, double[] x, double[] y) {
            int q = (x[i] >= cx ? 1 : 0) + (y[i] >= cy ? 2 : 0);
            if (children[q] == null) {
                double m = half / 2;
                children[q] = new Cell(cx + (q % 2 == 1 ? m : -m), cy + (q >= 2 ? m : -m), m);
            }
            return children[q];
        }

        void apply(int i, double[] x, double[] y, double[] vx, double[] vy, double strength) {
            if (totalMass == 0 || single == i) {
                return;
            }
            double dx = x[i] - mx;
            double dy = y[i] - my;
            double d2 = Math.max(100, dx * dx + dy * dy);
            if (children == null || (2 * half) * (2 * half) / d2 < THETA * THETA) {
                double k = strength * totalMass / d2;
                vx[i] += dx * k;
                vy[i] += dy * k;
                return;
            }
            for (Cell child : children) {
                if (child != null) {
                    child.apply(i, x, y, vx, vy, strength);
                }
            }
        }
    }
}

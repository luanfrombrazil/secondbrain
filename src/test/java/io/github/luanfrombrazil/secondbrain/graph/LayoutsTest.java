package io.github.luanfrombrazil.secondbrain.graph;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutsTest {

    private final Parameters p = new Parameters();

    private static Graph graph(List<String> ids, String... edges) {
        List<Node> nodes = ids.stream().map(id -> new Node(id, null, null)).toList();
        List<Edge> list = new ArrayList<>();
        for (String a : edges) {
            String[] parts = a.split(">");
            list.add(new Edge(parts[0], parts[1]));
        }
        return new Graph(nodes, list, List.of());
    }

    private static Graph randomAcyclic(int n) {
        java.util.Random random = new java.util.Random(7);
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            nodes.add(new Node("n" + i, "p" + i % 4, null));
            if (i > 0 && i % 10 != 0) {
                int parent = random.nextInt(i);
                edges.add(new Edge("n" + parent, "n" + i));
                if (i > 3 && random.nextBoolean()) {
                    int other = random.nextInt(i);
                    if (other != parent) {
                        edges.add(new Edge("n" + other, "n" + i));
                    }
                }
            }
        }
        return new Graph(nodes, edges, List.of());
    }

    private static void assertNoOverlap(Map<String, Point> positions) {
        List<Map.Entry<String, Point>> list = new ArrayList<>(positions.entrySet());
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Point a = list.get(i).getValue();
                Point b = list.get(j).getValue();
                boolean overlaps = Math.abs(a.x() - b.x()) < Node.WIDTH - 1e-6
                        && Math.abs(a.y() - b.y()) < Node.HEIGHT - 1e-6;
                assertFalse(overlaps, list.get(i).getKey() + " on top of " + list.get(j).getKey());
            }
        }
    }

    @Test
    void connectionThatClosesACycleIsRefused() {
        Graph g = graph(List.of("a", "b", "c"), "a>b", "b>c");
        assertFalse(g.canConnect("c", "a"));
        assertFalse(g.canConnect("a", "a"));
        assertFalse(g.canConnect("a", "b"));
        assertTrue(g.canConnect("a", "c"));
    }

    @Test
    void firstConnectionReleasesTheFixedPoint() {
        Graph g = new Graph(List.of(new Node("a", null, new Point(5, 5)), new Node("b", null, new Point(9, 9)),
                new Node("c", null, new Point(1, 1))), List.of(), List.of()).withConnection("a", "b");
        assertEquals(null, g.node("a").fixed());
        assertEquals(null, g.node("b").fixed());
        assertEquals(new Point(1, 1), g.node("c").fixed());
    }

    @Test
    void cycleIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> graph(List.of("a", "b", "c"), "a>b", "b>c", "c>a"));
    }

    @Test
    void eachCardIsRightOfItsDependencies() {
        Graph g = randomAcyclic(200);
        Map<String, Point> pos = new DependencyLayout().compute(g, p);
        for (Edge a : g.connections()) {
            assertTrue(pos.get(a.source()).x() < pos.get(a.target()).x());
        }
        assertNoOverlap(pos);
    }

    @Test
    void waitingCardComesAfterAllPrerequisites() {
        Graph g = graph(List.of("a", "b", "c", "d"), "a>b", "b>c", "a>d", "c>d");
        assertEquals(3, g.layer("d"));
    }

    @Test
    void onlyChildStaysOnParentRow() {
        Map<String, Point> pos = new DependencyLayout().compute(graph(List.of("a", "b", "c"), "a>b", "b>c"), p);
        assertEquals(pos.get("a").y(), pos.get("b").y(), 1e-9);
        assertEquals(pos.get("b").y(), pos.get("c").y(), 1e-9);
    }

    @Test
    void twoChildrenAreCenteredOnParent() {
        Map<String, Point> pos = new DependencyLayout().compute(graph(List.of("a", "b", "c"), "a>b", "a>c"), p);
        assertEquals(pos.get("a").y(), (pos.get("b").y() + pos.get("c").y()) / 2, 1e-9);
        assertTrue(pos.get("b").y() != pos.get("c").y());
    }

    @Test
    void threeChildrenAreAlsoCentered() {
        Map<String, Point> pos = new DependencyLayout().compute(graph(List.of("a", "b", "c", "d"), "a>b", "a>c", "a>d"), p);
        double smallest = Math.min(pos.get("b").y(), Math.min(pos.get("c").y(), pos.get("d").y()));
        double largest = Math.max(pos.get("b").y(), Math.max(pos.get("c").y(), pos.get("d").y()));
        assertEquals(pos.get("a").y(), (smallest + largest) / 2, 1e-9);
    }

    @Test
    void looseCardWithoutPointGoesToStrip() {
        Map<String, Point> pos = new DependencyLayout().compute(graph(List.of("a", "b", "loose"), "a>b"), p);
        assertTrue(pos.get("loose").y() > pos.get("a").y() + Node.HEIGHT);
    }

    @Test
    void fixedLooseCardStaysAndPushesNeighbours() {
        Graph base = graph(List.of("a", "b"), "a>b");
        Map<String, Point> before = new DependencyLayout().compute(base, p);
        Point point = before.get("a");
        Graph withLoose = base.withNode(new Node("loose", null, point));
        Map<String, Point> after = new DependencyLayout().compute(withLoose, p);
        assertEquals(point, after.get("loose"));
        assertNoOverlap(after);
    }

    @Test
    void radialKeepsEachLevelOnTheSameRadius() {
        Graph g = graph(List.of("r", "a", "b", "c", "d"), "r>a", "r>b", "a>c", "b>d");
        Map<String, Point> pos = new RadialLayout().compute(g, p);
        assertEquals(0, Math.hypot(pos.get("r").x(), pos.get("r").y()), 1e-9);
        assertEquals(Math.hypot(pos.get("a").x(), pos.get("a").y()), Math.hypot(pos.get("b").x(), pos.get("b").y()), 1e-6);
        assertEquals(Math.hypot(pos.get("c").x(), pos.get("c").y()), Math.hypot(pos.get("d").x(), pos.get("d").y()), 1e-6);
        assertNoOverlap(pos);
    }

    @Test
    void radialDoesNotOverlapOnLargeGraph() {
        assertNoOverlap(new RadialLayout().compute(randomAcyclic(200), p));
    }

    @Test
    void forcesAreDeterministicWithoutOverlap() {
        Graph g = randomAcyclic(100);
        Map<String, Point> a = new ForceLayout().compute(g, p);
        Map<String, Point> b = new ForceLayout().compute(g, p);
        assertEquals(a, b);
        assertNoOverlap(a);
    }

    @Test
    void forcesDoNotMoveFixedCard() {
        Graph g = graph(List.of("a", "b"), "a>b").withNode(new Node("loose", null, new Point(50, 50)));
        assertEquals(new Point(50, 50), new ForceLayout().compute(g, p).get("loose"));
    }
}

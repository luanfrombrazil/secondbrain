package io.github.luanfrombrazil.secondbrain.graph;

import io.github.luanfrombrazil.secondbrain.graph.layout.dependency.DependencyLayout;
import io.github.luanfrombrazil.secondbrain.graph.layout.force.ForceLayout;
import io.github.luanfrombrazil.secondbrain.graph.layout.radial.RadialLayout;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutsTest {


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
    void aChainNearFixedCardsKeepsItsOrderAndRow() {
        Graph g = new Graph(List.of(
                new Node("a", null, null), new Node("b", null, null), new Node("c", null, null),
                new Node("x", null, new Point(100, 80)), new Node("y", null, new Point(500, -60))),
                List.of(new Edge("a", "b"), new Edge("b", "c")), List.of());
        Map<String, Point> pos = new DependencyLayout().compute(g);
        assertEquals(pos.get("a").y(), pos.get("b").y(), 1e-9);
        assertEquals(pos.get("b").y(), pos.get("c").y(), 1e-9);
        assertTrue(pos.get("a").x() < pos.get("b").x());
        assertTrue(pos.get("b").x() < pos.get("c").x());
        assertEquals(new Point(100, 80), pos.get("x"));
        assertNoOverlap(pos);
    }

    @Test
    void aCardInTheWayOfASkippingConnectionStepsAside() {
        Map<String, Point> pos = new DependencyLayout().compute(graph(List.of("a", "b", "c"), "a>b", "b>c", "a>c"));
        assertTrue(pos.get("b").y() != pos.get("a").y());
        assertEquals((pos.get("a").y() + pos.get("b").y()) / 2, pos.get("c").y(), 1e-9);
        double lineAtB = pos.get("a").y() + (pos.get("c").y() - pos.get("a").y()) * 0.5;
        assertTrue(Math.abs(pos.get("b").y() - lineAtB) > Node.HEIGHT / 2);
        assertNoOverlap(pos);
    }

    @Test
    void childWithTwoParentsSitsBetweenThem() {
        Map<String, Point> pos = new DependencyLayout().compute(graph(List.of("a", "b", "c", "d"), "a>c", "b>c", "c>d"));
        assertEquals((pos.get("a").y() + pos.get("b").y()) / 2, pos.get("c").y(), 1e-9);
        assertEquals(pos.get("c").y(), pos.get("d").y(), 1e-9);
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
        Map<String, Point> pos = new DependencyLayout().compute(g);
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
        Map<String, Point> pos = new DependencyLayout().compute(graph(List.of("a", "b", "c"), "a>b", "b>c"));
        assertEquals(pos.get("a").y(), pos.get("b").y(), 1e-9);
        assertEquals(pos.get("b").y(), pos.get("c").y(), 1e-9);
    }

    @Test
    void twoChildrenAreCenteredOnParent() {
        Map<String, Point> pos = new DependencyLayout().compute(graph(List.of("a", "b", "c"), "a>b", "a>c"));
        assertEquals(pos.get("a").y(), (pos.get("b").y() + pos.get("c").y()) / 2, 1e-9);
        assertTrue(pos.get("b").y() != pos.get("c").y());
    }

    @Test
    void threeChildrenAreAlsoCentered() {
        Map<String, Point> pos = new DependencyLayout().compute(graph(List.of("a", "b", "c", "d"), "a>b", "a>c", "a>d"));
        double smallest = Math.min(pos.get("b").y(), Math.min(pos.get("c").y(), pos.get("d").y()));
        double largest = Math.max(pos.get("b").y(), Math.max(pos.get("c").y(), pos.get("d").y()));
        assertEquals(pos.get("a").y(), (smallest + largest) / 2, 1e-9);
    }

    @Test
    void looseCardWithoutPointGoesToStrip() {
        Map<String, Point> pos = new DependencyLayout().compute(graph(List.of("a", "b", "loose"), "a>b"));
        assertTrue(pos.get("loose").y() > pos.get("a").y() + Node.HEIGHT);
    }

    @Test
    void fixedLooseCardStaysAndPushesNeighbours() {
        Graph base = graph(List.of("a", "b"), "a>b");
        Map<String, Point> before = new DependencyLayout().compute(base);
        Point point = before.get("a");
        Graph withLoose = base.withNode(new Node("loose", null, point));
        Map<String, Point> after = new DependencyLayout().compute(withLoose);
        assertEquals(point, after.get("loose"));
        assertNoOverlap(after);
    }

    @Test
    void radialKeepsEachLevelOnTheSameRadius() {
        Graph g = graph(List.of("r", "a", "b", "c", "d"), "r>a", "r>b", "a>c", "b>d");
        Map<String, Point> pos = new RadialLayout().compute(g);
        assertEquals(0, Math.hypot(pos.get("r").x(), pos.get("r").y()), 1e-9);
        assertEquals(Math.hypot(pos.get("a").x(), pos.get("a").y()), Math.hypot(pos.get("b").x(), pos.get("b").y()), 1e-6);
        assertEquals(Math.hypot(pos.get("c").x(), pos.get("c").y()), Math.hypot(pos.get("d").x(), pos.get("d").y()), 1e-6);
        assertNoOverlap(pos);
    }

    @Test
    void radialDoesNotOverlapOnLargeGraph() {
        assertNoOverlap(new RadialLayout().compute(randomAcyclic(200)));
    }

    @Test
    void forcesAreDeterministicWithoutOverlap() {
        Graph g = randomAcyclic(100);
        Map<String, Point> a = new ForceLayout().compute(g);
        Map<String, Point> b = new ForceLayout().compute(g);
        assertEquals(a, b);
        assertNoOverlap(a);
    }

    @Test
    void forcesDoNotMoveFixedCard() {
        Graph g = graph(List.of("a", "b"), "a>b").withNode(new Node("loose", null, new Point(50, 50)));
        assertEquals(new Point(50, 50), new ForceLayout().compute(g).get("loose"));
    }
}

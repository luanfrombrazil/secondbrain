package io.github.luanfrombrazil.secondbrain.graph.lab;

import io.github.luanfrombrazil.secondbrain.graph.view.GraphRenderer;
import io.github.luanfrombrazil.secondbrain.graph.view.data.PointNote;
import io.github.luanfrombrazil.secondbrain.graph.view.data.CardData;
import io.github.luanfrombrazil.secondbrain.graph.Edge;
import io.github.luanfrombrazil.secondbrain.graph.Graph;
import io.github.luanfrombrazil.secondbrain.graph.Node;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Generates sample graphs for the lab. The same size always gives the same graph. */
public final class Samples {

    public record Sample(Graph graph, Map<String, CardData> cards, List<PointNote> pointNotes) {
    }

    private static final String[][] AREAS = {
            {"Matemática", "Cálculo", "Álgebra Linear", "EDO", "EDP", "Estatística", "Séries"},
            {"Programação", "Programação", "Algoritmos", "Banco de Dados", "Compiladores", "POO"},
            {"Hardware", "Circuitos", "Sistemas Digitais", "Arquitetura", "Sistemas Operacionais"},
            {"Redes", "Redes", "Sistemas Distribuídos", "Segurança"},
            {"IA e dados", "Ciência de Dados", "Inteligência Artificial", "Otimização"},
    };
    private static final double LOOSE_SHARE = 0.06;
    private static final double SECOND_PARENT_CHANCE = 0.25;
    private static final double LINK_CHANCE = 0.15;
    private static final int MAX_CHILDREN = 3;

    private Samples() {
    }

    public static Sample generate(int size) {
        Random random = new Random(42);
        List<Node> nodes = new ArrayList<>();
        List<Edge> connections = new ArrayList<>();
        List<Edge> links = new ArrayList<>();
        Map<String, CardData> cards = new LinkedHashMap<>();
        List<List<String>> byArea = new ArrayList<>();
        Map<String, Integer> childCount = new LinkedHashMap<>();
        for (int i = 0; i < AREAS.length; i++) {
            byArea.add(new ArrayList<>());
        }

        int looseCards = (int) Math.ceil(size * LOOSE_SHARE);
        for (int i = 0; i < size; i++) {
            String id = String.format("c%04d", i);
            childCount.put(id, 0);
            boolean loose = i >= size - looseCards;
            int area = i % AREAS.length;
            String[] names = AREAS[area];
            String title = names[1 + i % (names.length - 1)] + " " + roman(byArea.get(area).size() / (names.length - 1) + 1);
            nodes.add(new Node(id, loose ? null : names[0], null));
            cards.put(id, card(id, title, random));

            if (!loose) {
                List<String> sameArea = byArea.get(area);
                String parent = pickParent(sameArea, childCount, random);
                if (parent != null) {
                    connections.add(new Edge(parent, id));
                    childCount.merge(parent, 1, Integer::sum);
                }
                if (i > AREAS.length && random.nextDouble() < SECOND_PARENT_CHANCE) {
                    String other = String.format("c%04d", random.nextInt(i));
                    boolean duplicate = other.equals(parent);
                    boolean isLoose = nodes.stream().anyMatch(n -> n.id().equals(other) && n.folder() == null);
                    if (!duplicate && !isLoose) {
                        connections.add(new Edge(other, id));
                    }
                }
                if (i > 2 && random.nextDouble() < LINK_CHANCE) {
                    links.add(new Edge(String.format("c%04d", random.nextInt(i)), id));
                }
                sameArea.add(id);
            }
        }

        List<PointNote> pointNotes = new ArrayList<>();
        for (int i = 0; i < size; i += 2) {
            String id = String.format("c%04d", i);
            pointNotes.add(new PointNote("O Andar do Bêbado", List.of(id)));
            pointNotes.add(new PointNote("Artigo " + (i + 1) + ".pdf", List.of(id)));
        }
        return new Sample(new Graph(nodes, connections, links), cards, pointNotes);
    }

    private static String pickParent(List<String> candidates, Map<String, Integer> childCount, Random random) {
        List<String> available = candidates.stream().filter(c -> childCount.get(c) < MAX_CHILDREN).toList();
        if (available.isEmpty()) {
            return null;
        }
        // Prefers the most recent, so the trees gain depth.
        int recent = Math.min(available.size(), 4);
        return available.get(available.size() - 1 - random.nextInt(recent));
    }

    private static CardData card(String id, String title, Random random) {
        double progress;
        Color color;
        switch (random.nextInt(4)) {
            case 0 -> {
                color = GraphRenderer.COLOR_DONE;
                progress = 1;
            }
            case 1 -> {
                color = GraphRenderer.COLOR_IN_PROGRESS;
                progress = 0.2 + random.nextDouble() * 0.7;
            }
            case 2 -> {
                color = GraphRenderer.COLOR_AVAILABLE;
                progress = 0;
            }
            default -> {
                color = GraphRenderer.COLOR_BLOCKED;
                progress = 0;
            }
        }
        List<String> tasks = List.of("Lista 1", "Lista 2", "Revisão");
        int done = (int) Math.round(progress * tasks.size());
        return new CardData(id, title, color, progress, 0.2 + random.nextDouble() * 0.8, tasks, done, random.nextInt(4) == 0, true);
    }

    private static String roman(int n) {
        String[] numerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return n <= numerals.length ? numerals[n - 1] : String.valueOf(n);
    }
}

package io.github.luanfrombrazil.secondbrain.app;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.graph.Edge;
import io.github.luanfrombrazil.secondbrain.graph.Graph;
import io.github.luanfrombrazil.secondbrain.graph.Node;
import io.github.luanfrombrazil.secondbrain.graph.Point;
import io.github.luanfrombrazil.secondbrain.graph.view.data.CardData;
import io.github.luanfrombrazil.secondbrain.property.ProgressProperty;
import io.github.luanfrombrazil.secondbrain.property.StateProperty;
import io.github.luanfrombrazil.secondbrain.property.TasksProperty;
import io.github.luanfrombrazil.secondbrain.track.StateOption;
import io.github.luanfrombrazil.secondbrain.track.Track;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Turns one level of a track into what the graph view shows. The graph never sees a Card. */
final class TrackGraphBridge {

    private static final Color NO_STATE = Color.web("#6b7280");
    private static final Color ERROR = Color.web("#e0524d");
    private static final double FULL_WEIGHT = 8;

    record View(Graph graph, Map<String, CardData> cards) {
    }

    private TrackGraphBridge() {
    }

    static View build(Track track, List<Card> level) {
        Set<String> ids = new HashSet<>();
        level.forEach(card -> ids.add(card.id()));
        List<Node> nodes = new ArrayList<>();
        List<Edge> connections = new ArrayList<>();
        List<Edge> links = new ArrayList<>();
        Map<String, CardData> cards = new LinkedHashMap<>();
        for (Card card : level) {
            double[] fixed = card.fixedPoint();
            nodes.add(new Node(card.id(), null, fixed == null ? null : new Point(fixed[0], fixed[1])));
            card.dependencies().stream().filter(ids::contains)
                    .forEach(dependency -> connections.add(new Edge(dependency, card.id())));
            card.links().stream().filter(ids::contains)
                    .forEach(link -> links.add(new Edge(card.id(), link)));
            cards.put(card.id(), data(track, card));
        }
        return new View(new Graph(nodes, connections, links), cards);
    }

    private static CardData data(Track track, Card card) {
        List<String> tasks = card.property(TasksProperty.class)
                .map(p -> p.tasks().stream().map(TasksProperty.Task::text).toList())
                .orElse(List.of());
        int done = card.property(TasksProperty.class).map(TasksProperty::doneCount).orElse(0);
        return new CardData(card.id(), card.title(), color(track, card), progress(track, card, tasks.size(), done),
                Math.min(1, (card.properties().size() + card.dependencies().size()) / FULL_WEIGHT),
                tasks, done, !card.innerCards().isEmpty());
    }

    private static Color color(Track track, Card card) {
        return card.property(StateProperty.class)
                .map(state -> track.option(state.optionId()).map(o -> Color.web(o.color())).orElse(ERROR))
                .orElse(NO_STATE);
    }

    /** Finished tasks plus finished inner cards. Zero for a card without the progress property. */
    private static double progress(Track track, Card card, int tasks, int done) {
        if (card.property(ProgressProperty.class).isEmpty()) {
            return 0;
        }
        int innerDone = (int) card.innerCards().stream()
                .filter(inner -> inner.property(StateProperty.class)
                        .flatMap(state -> track.option(state.optionId()))
                        .filter(option -> option.category() == StateOption.Category.DONE).isPresent())
                .count();
        int total = tasks + card.innerCards().size();
        return total == 0 ? 0 : (double) (done + innerDone) / total;
    }
}

package io.github.luanfrombrazil.secondbrain.app;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.property.StateProperty;
import io.github.luanfrombrazil.secondbrain.track.Connections;
import io.github.luanfrombrazil.secondbrain.track.StateRules;
import io.github.luanfrombrazil.secondbrain.track.Track;
import io.github.luanfrombrazil.secondbrain.vault.Vault;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Applies the user's edits to one level of a track: saves them, runs the automatic state
 * rules and keeps the undo history. Knows nothing about the screen.
 */
public final class LevelEditor {

    private interface Step {
        void run() throws IOException;
    }

    private record StateValue(String option, String previous) {
    }

    private record Edit(Step apply, Step revert, Map<StateProperty, StateValue> statesBefore) {
    }

    private final Vault vault;
    private final Track track;
    private final Deque<Edit> undoStack = new ArrayDeque<>();
    private final Deque<Edit> redoStack = new ArrayDeque<>();
    private Card parent;

    public LevelEditor(Vault vault, Track track) throws IOException {
        this.vault = vault;
        this.track = track;
        saveRuleChanges();
    }

    public Track track() {
        return track;
    }

    /** The card whose inner graph is open, or null at the root of the track. */
    public Card parent() {
        return parent;
    }

    public void open(Card newParent) {
        parent = newParent;
    }

    public List<Card> level() {
        return parent == null ? track.cards() : parent.innerCards();
    }

    public Card createCard(String title, double[] fixedPoint) throws IOException {
        Card owner = parent;
        Card[] created = new Card[1];
        perform(new Edit(
                () -> {
                    if (created[0] == null) {
                        created[0] = vault.createCard(track, owner, title);
                        created[0].setFixedPoint(fixedPoint);
                        vault.saveCard(track, created[0], owner);
                    } else {
                        vault.restoreCard(track, owner, created[0], new byte[0]);
                    }
                },
                () -> vault.deleteCard(track, owner, created[0]),
                snapshot()));
        return created[0];
    }

    /** @return false if the connection is not allowed; nothing changes then */
    public boolean connect(String prerequisiteId, String dependentId) throws IOException {
        List<Card> level = level();
        Card prerequisite = find(level, prerequisiteId);
        Card dependent = find(level, dependentId);
        if (!Connections.canConnect(level, prerequisite, dependent)) {
            return false;
        }
        double[] prerequisitePoint = prerequisite.fixedPoint();
        double[] dependentPoint = dependent.fixedPoint();
        List<Card> touched = List.of(prerequisite, dependent);
        perform(new Edit(
                () -> {
                    Connections.connect(level, prerequisite, dependent);
                    vault.saveCards(track, touched);
                },
                () -> {
                    Connections.disconnect(prerequisite, dependent);
                    prerequisite.setFixedPoint(prerequisitePoint);
                    dependent.setFixedPoint(dependentPoint);
                    vault.saveCards(track, touched);
                },
                snapshot()));
        return true;
    }

    /** @return false for a card that has an inner graph, which cannot be deleted yet */
    public boolean deleteCard(String id) throws IOException {
        Card owner = parent;
        List<Card> level = level();
        Card card = find(level, id);
        if (!card.innerCards().isEmpty()) {
            return false;
        }
        List<Card> dependents = level.stream().filter(c -> c.dependencies().contains(id)).toList();
        byte[][] note = new byte[1][];
        perform(new Edit(
                () -> {
                    note[0] = vault.deleteCard(track, owner, card);
                    dependents.forEach(c -> c.dependencies().remove(id));
                    vault.saveCards(track, dependents);
                },
                () -> {
                    vault.restoreCard(track, owner, card, note[0]);
                    dependents.forEach(c -> c.dependencies().add(id));
                    vault.saveCards(track, dependents);
                },
                snapshot()));
        return true;
    }

    public boolean undo() throws IOException {
        if (undoStack.isEmpty()) {
            return false;
        }
        Edit edit = undoStack.pop();
        Map<StateProperty, StateValue> current = snapshot();
        edit.revert().run();
        restore(edit.statesBefore());
        saveStatesThatDiffer(current);
        redoStack.push(edit);
        return true;
    }

    public boolean redo() throws IOException {
        if (redoStack.isEmpty()) {
            return false;
        }
        Edit edit = redoStack.pop();
        edit.apply().run();
        saveRuleChanges();
        undoStack.push(edit);
        return true;
    }

    private void perform(Edit edit) throws IOException {
        edit.apply().run();
        saveRuleChanges();
        undoStack.push(edit);
        redoStack.clear();
    }

    private void saveRuleChanges() throws IOException {
        Set<Card> changed = new HashSet<>();
        StateRules.reevaluate(track).forEach(change -> changed.add(change.card()));
        vault.saveCards(track, changed);
    }

    private Map<StateProperty, StateValue> snapshot() {
        Map<StateProperty, StateValue> values = new HashMap<>();
        forEachCard(track.cards(), card -> card.property(StateProperty.class).ifPresent(
                state -> values.put(state, new StateValue(state.optionId(), state.previousOptionId()))));
        return values;
    }

    private static void restore(Map<StateProperty, StateValue> values) {
        values.forEach((state, value) -> {
            state.setOptionId(value.option());
            state.setPreviousOptionId(value.previous());
        });
    }

    private void saveStatesThatDiffer(Map<StateProperty, StateValue> before) throws IOException {
        List<Card> changed = new ArrayList<>();
        forEachCard(track.cards(), card -> card.property(StateProperty.class).ifPresent(state -> {
            StateValue old = before.get(state);
            if (old == null || !Objects.equals(old.option(), state.optionId())
                    || !Objects.equals(old.previous(), state.previousOptionId())) {
                changed.add(card);
            }
        }));
        vault.saveCards(track, changed);
    }

    private static void forEachCard(List<Card> level, java.util.function.Consumer<Card> action) {
        for (Card card : level) {
            action.accept(card);
            forEachCard(card.innerCards(), action);
        }
    }

    private static Card find(List<Card> level, String id) {
        return level.stream().filter(c -> c.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown card: " + id));
    }
}

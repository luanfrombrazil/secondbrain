package io.github.luanfrombrazil.secondbrain.app;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.card.CardType;
import io.github.luanfrombrazil.secondbrain.property.ProgressProperty;
import io.github.luanfrombrazil.secondbrain.property.Properties;
import io.github.luanfrombrazil.secondbrain.property.Property;
import io.github.luanfrombrazil.secondbrain.property.SelectionProperty;
import io.github.luanfrombrazil.secondbrain.property.StateProperty;
import io.github.luanfrombrazil.secondbrain.property.TasksProperty;
import io.github.luanfrombrazil.secondbrain.property.TasksProperty.Task;
import io.github.luanfrombrazil.secondbrain.property.TextProperty;
import io.github.luanfrombrazil.secondbrain.track.Connections;
import io.github.luanfrombrazil.secondbrain.track.Selection;
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
import java.util.UUID;
import java.util.function.Consumer;

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

    private record StoredProperty(String kind, Map<String, Object> data) {
    }

    /**
     * @param chosenByUser a card whose state the user has just picked: the rules leave it alone
     */
    private record Edit(Step apply, Step revert, Map<Card, StateValue> statesBefore, Card chosenByUser) {
    }

    private final Vault vault;
    private final Track track;
    private final Deque<Edit> undoStack = new ArrayDeque<>();
    private final Deque<Edit> redoStack = new ArrayDeque<>();
    private Card parent;

    public LevelEditor(Vault vault, Track track) throws IOException {
        this.vault = vault;
        this.track = track;
        saveRuleChanges(null);
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
        byte[][] note = new byte[1][];
        perform(new Edit(
                () -> {
                    if (created[0] == null) {
                        created[0] = vault.createCard(track, owner, title);
                        created[0].setFixedPoint(fixedPoint);
                        vault.saveCard(track, created[0], owner);
                    } else {
                        vault.restoreCard(track, owner, created[0], note[0]);
                    }
                },
                () -> note[0] = vault.deleteCard(track, owner, created[0]),
                snapshot(), null));
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
                snapshot(), null));
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
                snapshot(), null));
        return true;
    }

    public void setTitle(Card card, String title) throws IOException {
        editCard(card, c -> c.setTitle(title), false);
    }

    public void setType(Card card, String typeId) throws IOException {
        editCard(card, c -> c.setTypeId(typeId), false);
    }

    /** @param optionId the new state, or null for a card with no state */
    public void setState(Card card, String optionId) throws IOException {
        editCard(card, c -> {
            c.properties().removeIf(StateProperty.class::isInstance);
            if (optionId != null) {
                c.properties().add(new StateProperty(optionId));
            }
        }, true);
    }

    public void setProgressShown(Card card, boolean shown) throws IOException {
        editCard(card, c -> {
            c.properties().removeIf(ProgressProperty.class::isInstance);
            if (shown) {
                c.properties().add(new ProgressProperty());
            }
        }, false);
    }

    public void addTask(Card card, String text) throws IOException {
        editTasks(card, tasks -> tasks.add(new Task(text, false)));
    }

    public void setTaskDone(Card card, int index, boolean done) throws IOException {
        editTasks(card, tasks -> tasks.set(index, new Task(tasks.get(index).text(), done)));
    }

    public void removeTask(Card card, int index) throws IOException {
        editTasks(card, tasks -> tasks.remove(index));
    }

    public void addText(Card card, String name, String value) throws IOException {
        editCard(card, c -> c.properties().add(new TextProperty(name, value)), false);
    }

    public void removeProperty(Card card, Property property) throws IOException {
        int index = card.properties().indexOf(property);
        editCard(card, c -> c.properties().remove(index), false);
    }

    /** @param optionId the chosen option, or null to clear this selection on the card */
    public void setSelection(Card card, String selectionId, String optionId) throws IOException {
        editCard(card, c -> {
            c.properties().removeIf(p -> p instanceof SelectionProperty s && s.selectionId().equals(selectionId));
            if (optionId != null) {
                c.properties().add(new SelectionProperty(selectionId, optionId));
            }
        }, false);
    }

    public CardType createType(String name) throws IOException {
        CardType type = new CardType(UUID.randomUUID().toString(), name, "", List.of());
        track.types().add(type);
        vault.saveTrack(track);
        return type;
    }

    public Selection createSelection(String name, List<String> optionNames) throws IOException {
        List<Selection.Option> options = optionNames.stream()
                .map(option -> new Selection.Option(UUID.randomUUID().toString(), option))
                .toList();
        Selection selection = new Selection(UUID.randomUUID().toString(), name, options);
        track.selections().add(selection);
        vault.saveTrack(track);
        return selection;
    }

    public boolean undo() throws IOException {
        if (undoStack.isEmpty()) {
            return false;
        }
        Edit edit = undoStack.pop();
        Map<Card, StateValue> current = snapshot();
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
        saveRuleChanges(edit.chosenByUser());
        undoStack.push(edit);
        return true;
    }

    private void editTasks(Card card, Consumer<List<Task>> change) throws IOException {
        editCard(card, c -> {
            TasksProperty tasks = c.property(TasksProperty.class).orElseGet(() -> {
                TasksProperty created = new TasksProperty();
                c.properties().add(created);
                return created;
            });
            change.accept(tasks.tasks());
            StateRules.tasksChanged(track, c);
        }, false);
    }

    private void editCard(Card card, Consumer<Card> change, boolean stateChosenByUser) throws IOException {
        String title = card.title();
        String typeId = card.typeId();
        List<StoredProperty> properties = new ArrayList<>();
        card.properties().forEach(p -> properties.add(new StoredProperty(p.kind(), p.toData())));
        List<Card> touched = List.of(card);
        perform(new Edit(
                () -> {
                    change.accept(card);
                    vault.saveCards(track, touched);
                },
                () -> {
                    card.setTitle(title);
                    card.setTypeId(typeId);
                    card.properties().clear();
                    properties.forEach(p -> Properties.fromData(p.kind(), p.data()).ifPresent(card.properties()::add));
                    vault.saveCards(track, touched);
                },
                snapshot(), stateChosenByUser ? card : null));
    }

    private void perform(Edit edit) throws IOException {
        edit.apply().run();
        saveRuleChanges(edit.chosenByUser());
        undoStack.push(edit);
        redoStack.clear();
    }

    private void saveRuleChanges(Card chosenByUser) throws IOException {
        Set<Card> changed = new HashSet<>();
        StateRules.reevaluate(track, chosenByUser).forEach(change -> changed.add(change.card()));
        vault.saveCards(track, changed);
    }

    private Map<Card, StateValue> snapshot() {
        Map<Card, StateValue> values = new HashMap<>();
        forEachCard(track.cards(), card -> card.property(StateProperty.class).ifPresent(
                state -> values.put(card, new StateValue(state.optionId(), state.previousOptionId()))));
        return values;
    }

    private static void restore(Map<Card, StateValue> values) {
        values.forEach((card, value) -> card.property(StateProperty.class).ifPresent(state -> {
            state.setOptionId(value.option());
            state.setPreviousOptionId(value.previous());
        }));
    }

    private void saveStatesThatDiffer(Map<Card, StateValue> before) throws IOException {
        List<Card> changed = new ArrayList<>();
        forEachCard(track.cards(), card -> card.property(StateProperty.class).ifPresent(state -> {
            StateValue old = before.get(card);
            if (old == null || !Objects.equals(old.option(), state.optionId())
                    || !Objects.equals(old.previous(), state.previousOptionId())) {
                changed.add(card);
            }
        }));
        vault.saveCards(track, changed);
    }

    private static void forEachCard(List<Card> level, Consumer<Card> action) {
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

package io.github.luanfrombrazil.secondbrain.track;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.property.ProgressProperty;
import io.github.luanfrombrazil.secondbrain.property.StateProperty;
import io.github.luanfrombrazil.secondbrain.property.TasksProperty;
import io.github.luanfrombrazil.secondbrain.track.StateOption.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** The automatic state changes (D-40 to D-46). States inform; nothing here ever locks a card. */
public final class StateRules {

    /** @param from the option before the change */
    public record Change(Card card, String from, String to) {
    }

    private StateRules() {
    }

    /** The dependency rules for the whole track. Same as {@link #reevaluate(Track, Card)} with no exception. */
    public static List<Change> reevaluate(Track track) {
        return reevaluate(track, null);
    }

    /**
     * Applies the dependency rules and returns what changed.
     *
     * @param chosenByUser a card whose state the user has just picked: it stays as chosen, or null
     */
    public static List<Change> reevaluate(Track track, Card chosenByUser) {
        List<Change> changes = new ArrayList<>();
        applyBlocking(track, track.cards(), false, chosenByUser, changes);
        return changes;
    }

    /**
     * The checklist of this card changed: the first task starts it, the last one finishes it,
     * and unchecking reopens it. This only runs on a change, so a card the user forced to
     * Done before 100% stays Done.
     */
    public static List<Change> tasksChanged(Track track, Card card) {
        List<Change> changes = new ArrayList<>();
        Optional<StateProperty> state = card.property(StateProperty.class);
        Optional<TasksProperty> tasks = card.property(TasksProperty.class);
        if (state.isEmpty() || tasks.isEmpty() || card.property(ProgressProperty.class).isEmpty()) {
            return changes;
        }
        Category category = categoryOf(track, state.get());
        int done = tasks.get().doneCount();
        int total = tasks.get().tasks().size();
        if (total == 0) {
            return changes;
        }
        if ((category == Category.AVAILABLE || category == Category.IN_PROGRESS) && done == total) {
            moveTo(track, card, state.get(), Category.DONE, changes);
        } else if (category == Category.AVAILABLE && done > 0) {
            moveTo(track, card, state.get(), Category.IN_PROGRESS, changes);
        } else if (category == Category.DONE && done < total) {
            moveTo(track, card, state.get(), Category.IN_PROGRESS, changes);
        }
        return changes;
    }

    private static void applyBlocking(Track track, List<Card> level, boolean parentBlocked, Card chosenByUser,
                                      List<Change> changes) {
        Map<String, Card> byId = new HashMap<>();
        level.forEach(card -> byId.put(card.id(), card));
        for (Card card : level) {
            boolean blocked = parentBlocked || hasPendingDependency(track, card, byId);
            card.property(StateProperty.class).filter(state -> card != chosenByUser).ifPresent(state -> {
                Category category = categoryOf(track, state);
                if (blocked && (category == Category.AVAILABLE || category == Category.IN_PROGRESS)) {
                    String previous = state.optionId();
                    if (moveTo(track, card, state, Category.BLOCKED, changes)) {
                        state.setPreviousOptionId(previous);
                    }
                } else if (!blocked && category == Category.BLOCKED) {
                    release(track, card, state, changes);
                }
            });
            applyBlocking(track, card.innerCards(), blocked, chosenByUser, changes);
        }
    }

    /** A dependency without a state, or missing from this level, blocks nobody (D-45). */
    private static boolean hasPendingDependency(Track track, Card card, Map<String, Card> byId) {
        return card.dependencies().stream()
                .map(byId::get)
                .filter(dependency -> dependency != null)
                .map(dependency -> dependency.property(StateProperty.class))
                .flatMap(Optional::stream)
                .anyMatch(state -> categoryOf(track, state) != Category.DONE);
    }

    /** Goes back to the option held before the block, or to the first available one (D-41). */
    private static void release(Track track, Card card, StateProperty state, List<Change> changes) {
        String previous = state.previousOptionId();
        if (previous != null && track.option(previous).isPresent()) {
            changes.add(new Change(card, state.optionId(), previous));
            state.setOptionId(previous);
        } else {
            moveTo(track, card, state, Category.AVAILABLE, changes);
        }
        state.setPreviousOptionId(null);
    }

    private static boolean moveTo(Track track, Card card, StateProperty state, Category category, List<Change> changes) {
        Optional<StateOption> option = track.firstOption(category);
        option.ifPresent(o -> {
            changes.add(new Change(card, state.optionId(), o.id()));
            state.setOptionId(o.id());
        });
        return option.isPresent();
    }

    /** Null when the option was deleted: the card is then in error and the rules leave it alone. */
    private static Category categoryOf(Track track, StateProperty state) {
        return track.option(state.optionId()).map(StateOption::category).orElse(null);
    }
}

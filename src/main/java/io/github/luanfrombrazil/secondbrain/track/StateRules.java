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

    /** Applies the rules to the whole track and returns what changed. */
    public static List<Change> reevaluate(Track track) {
        List<Change> changes = new ArrayList<>();
        applyProgress(track, track.cards(), changes);
        applyBlocking(track, track.cards(), false, changes);
        return changes;
    }

    private static void applyProgress(Track track, List<Card> level, List<Change> changes) {
        for (Card card : level) {
            applyProgress(track, card.innerCards(), changes);
            Optional<StateProperty> state = card.property(StateProperty.class);
            Optional<TasksProperty> tasks = card.property(TasksProperty.class);
            if (state.isEmpty() || tasks.isEmpty() || tasks.get().tasks().isEmpty()
                    || card.property(ProgressProperty.class).isEmpty()) {
                continue;
            }
            Category category = categoryOf(track, state.get());
            int done = tasks.get().doneCount();
            int total = tasks.get().tasks().size();
            if (category == Category.AVAILABLE && done > 0 && done < total) {
                moveTo(track, card, state.get(), Category.IN_PROGRESS, changes);
            } else if ((category == Category.AVAILABLE || category == Category.IN_PROGRESS) && done == total) {
                moveTo(track, card, state.get(), Category.DONE, changes);
            } else if (category == Category.DONE && done < total) {
                moveTo(track, card, state.get(), Category.IN_PROGRESS, changes);
            }
        }
    }

    private static void applyBlocking(Track track, List<Card> level, boolean parentBlocked, List<Change> changes) {
        Map<String, Card> byId = new HashMap<>();
        level.forEach(card -> byId.put(card.id(), card));
        for (Card card : level) {
            boolean blocked = parentBlocked || hasPendingDependency(track, card, byId);
            card.property(StateProperty.class).ifPresent(state -> {
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
            applyBlocking(track, card.innerCards(), blocked, changes);
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

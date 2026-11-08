package io.github.luanfrombrazil.secondbrain.track;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.property.ProgressProperty;
import io.github.luanfrombrazil.secondbrain.property.StateProperty;
import io.github.luanfrombrazil.secondbrain.property.TasksProperty;
import io.github.luanfrombrazil.secondbrain.property.TasksProperty.Task;
import io.github.luanfrombrazil.secondbrain.track.StateOption.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackRulesTest {

    private Track track;

    @BeforeEach
    void setUp() {
        track = new Track("t", "Trilha", "#fff", "trilha");
        track.states().add(new StateOption("blocked", "Bloqueado", "#f00", Category.BLOCKED));
        track.states().add(new StateOption("available", "Disponível", "#ff0", Category.AVAILABLE));
        track.states().add(new StateOption("studying", "Estudando", "#00f", Category.IN_PROGRESS));
        track.states().add(new StateOption("review", "Revisar", "#00a", Category.IN_PROGRESS));
        track.states().add(new StateOption("done", "Concluído", "#0f0", Category.DONE));
    }

    private Card card(String id, String state) {
        Card card = new Card(id, id, id + ".tex");
        if (state != null) {
            card.properties().add(new StateProperty(state));
        }
        track.cards().add(card);
        return card;
    }

    private static String state(Card card) {
        return card.property(StateProperty.class).orElseThrow().optionId();
    }

    private static void withTasks(Card card, boolean... done) {
        TasksProperty tasks = new TasksProperty();
        for (boolean each : done) {
            tasks.tasks().add(new Task("x", each));
        }
        card.properties().add(tasks);
        card.properties().add(new ProgressProperty());
    }

    @Test
    void pendingDependencyBlocksAvailableAndInProgress() {
        Card a = card("a", "available");
        Card b = card("b", "available");
        Card c = card("c", "review");
        b.dependencies().add("a");
        c.dependencies().add("a");
        StateRules.reevaluate(track);
        assertEquals("available", state(a));
        assertEquals("blocked", state(b));
        assertEquals("blocked", state(c));
    }

    @Test
    void doneCardNeverGoesBackToBlocked() {
        card("a", "available");
        Card b = card("b", "done");
        b.dependencies().add("a");
        StateRules.reevaluate(track);
        assertEquals("done", state(b));
    }

    @Test
    void finishingTheDependencyRestoresThePreviousState() {
        Card a = card("a", "available");
        Card b = card("b", "review");
        b.dependencies().add("a");
        StateRules.reevaluate(track);
        a.property(StateProperty.class).orElseThrow().setOptionId("done");
        StateRules.reevaluate(track);
        assertEquals("review", state(b));
        assertNull(b.property(StateProperty.class).orElseThrow().previousOptionId());
    }

    @Test
    void cardBornBlockedBecomesAvailable() {
        Card a = card("a", "done");
        Card b = card("b", "blocked");
        b.dependencies().add("a");
        StateRules.reevaluate(track);
        assertEquals("available", state(b));
    }

    @Test
    void cardWithoutStateBlocksNobody() {
        card("a", null);
        Card b = card("b", "available");
        b.dependencies().add("a");
        StateRules.reevaluate(track);
        assertEquals("available", state(b));
    }

    @Test
    void blockedCardWithAllTasksDoneStaysBlocked() {
        card("a", "available");
        Card b = card("b", "blocked");
        withTasks(b, true, true);
        b.dependencies().add("a");
        StateRules.reevaluate(track);
        assertEquals("blocked", state(b));
    }

    @Test
    void firstTaskStartsTheCardUsingTheFirstOptionOfTheCategory() {
        Card a = card("a", "available");
        withTasks(a, true, false);
        StateRules.tasksChanged(track, a);
        assertEquals("studying", state(a));
    }

    @Test
    void allTasksFinishTheCardAndUncheckingReopensIt() {
        Card a = card("a", "studying");
        withTasks(a, true, true);
        StateRules.tasksChanged(track, a);
        assertEquals("done", state(a));
        a.property(TasksProperty.class).orElseThrow().tasks().set(0, new Task("x", false));
        StateRules.tasksChanged(track, a);
        assertEquals("studying", state(a));
    }

    @Test
    void cardForcedDoneBeforeAllTasksStaysDone() {
        Card a = card("a", "done");
        withTasks(a, true, false);
        StateRules.reevaluate(track);
        assertEquals("done", state(a));
    }

    @Test
    void stateChosenByTheUserIsNotOverwrittenByTheRules() {
        card("a", "available");
        Card b = card("b", "available");
        b.dependencies().add("a");
        StateRules.reevaluate(track, b);
        assertEquals("available", state(b));
    }

    @Test
    void cardWithNoTasksIsNotFinished() {
        Card a = card("a", "available");
        withTasks(a);
        StateRules.tasksChanged(track, a);
        assertEquals("available", state(a));
    }

    @Test
    void innerCardsInheritTheBlockOfTheirParent() {
        card("a", "available");
        Card b = card("b", "available");
        b.dependencies().add("a");
        Card inner = new Card("i", "i", "i.tex");
        inner.properties().add(new StateProperty("studying"));
        b.innerCards().add(inner);
        StateRules.reevaluate(track);
        assertEquals("blocked", state(inner));
    }

    @Test
    void deletedOptionLeavesTheCardAlone() {
        card("a", "available");
        Card b = card("b", "gone");
        b.dependencies().add("a");
        assertTrue(StateRules.reevaluate(track).isEmpty());
    }

    @Test
    void connectionsThatCloseACycleAreRefused() {
        Card a = card("a", "available");
        Card b = card("b", "available");
        Card c = card("c", "available");
        List<Card> level = track.cards();
        Connections.connect(level, a, b);
        Connections.connect(level, b, c);
        assertFalse(Connections.canConnect(level, c, a));
        assertFalse(Connections.canConnect(level, a, a));
        assertFalse(Connections.canConnect(level, a, b));
        assertTrue(Connections.canConnect(level, a, c));
    }

    @Test
    void firstConnectionReleasesTheFixedPoint() {
        Card a = card("a", "available");
        Card b = card("b", "available");
        a.setFixedPoint(new double[]{1, 2});
        Connections.connect(track.cards(), a, b);
        assertNull(a.fixedPoint());
    }

    @Test
    void removingACardRewiresTheChain() {
        Card calculus1 = card("c1", "done");
        Card calculus2 = card("c2", "available");
        Card calculus3 = card("c3", "available");
        calculus2.dependencies().add("c1");
        calculus3.dependencies().add("c2");
        Connections.removeKeepingChain(track.cards(), calculus2);
        assertEquals(Set.of("c1"), calculus3.dependencies());
        assertTrue(calculus2.dependencies().isEmpty());
    }
}

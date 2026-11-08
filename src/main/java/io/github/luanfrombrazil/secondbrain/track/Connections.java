package io.github.luanfrombrazil.secondbrain.track;

import io.github.luanfrombrazil.secondbrain.card.Card;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The dependency connections among the cards of one level of a graph. */
public final class Connections {

    private Connections() {
    }

    /** False for a card onto itself, a repeated connection, or one that would close a cycle (D-52). */
    public static boolean canConnect(List<Card> level, Card prerequisite, Card dependent) {
        return prerequisite != dependent
                && !dependent.dependencies().contains(prerequisite.id())
                && !reaches(level, prerequisite, dependent.id());
    }

    /** Whether "from" depends, directly or not, on the card with this id. */
    private static boolean reaches(List<Card> level, Card from, String id) {
        Map<String, Card> byId = index(level);
        Set<String> seen = new HashSet<>();
        ArrayDeque<Card> pending = new ArrayDeque<>(List.of(from));
        while (!pending.isEmpty()) {
            Card card = pending.pop();
            if (card.id().equals(id)) {
                return true;
            }
            if (seen.add(card.id())) {
                card.dependencies().stream().map(byId::get).filter(c -> c != null).forEach(pending::add);
            }
        }
        return false;
    }

    /** The first connection releases the fixed point; the layout arranges the two cards from then on. */
    public static void connect(List<Card> level, Card prerequisite, Card dependent) {
        if (!canConnect(level, prerequisite, dependent)) {
            throw new IllegalArgumentException("Invalid connection: " + prerequisite.id() + " -> " + dependent.id());
        }
        dependent.dependencies().add(prerequisite.id());
        prerequisite.setFixedPoint(null);
        dependent.setFixedPoint(null);
    }

    public static void disconnect(Card prerequisite, Card dependent) {
        dependent.dependencies().remove(prerequisite.id());
    }

    /**
     * Takes a card out of the chain: its prerequisites pass to its dependents, so removing
     * Calculus II makes Calculus I a prerequisite of Calculus III (D-51).
     */
    public static void removeKeepingChain(List<Card> level, Card card) {
        for (Card other : level) {
            if (other.dependencies().remove(card.id())) {
                other.dependencies().addAll(card.dependencies());
            }
        }
        card.dependencies().clear();
    }

    private static Map<String, Card> index(List<Card> level) {
        Map<String, Card> byId = new HashMap<>();
        level.forEach(card -> byId.put(card.id(), card));
        return byId;
    }
}

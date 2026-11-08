package io.github.luanfrombrazil.secondbrain.graph.layout.common;

import io.github.luanfrombrazil.secondbrain.graph.Node;
import io.github.luanfrombrazil.secondbrain.graph.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Moves cards out from under the fixed ones. A group of connected cards moves as one piece,
 * so the arrangement the layout chose is never scattered.
 */
public final class MagneticRepulsion {

    private static final int PASSES = 40;
    private static final double CARD_GAP = 16;

    private MagneticRepulsion() {
    }

    /** @param groups the sets of cards that must keep their relative positions; each card in at most one */
    public static Map<String, Point> apply(Map<String, Point> positions, Set<String> fixedIds,
                                           double radius, Collection<Set<String>> groups) {
        if (fixedIds.isEmpty()) {
            return positions;
        }
        Map<String, Point> output = new HashMap<>(positions);
        List<Set<String>> movable = movableGroups(positions, fixedIds, groups);

        double fixedWidth = Node.WIDTH + radius;
        double fixedHeight = Node.HEIGHT + radius;
        Set<Set<String>> pushed = new HashSet<>();
        for (String fixed : fixedIds) {
            Point center = output.get(fixed);
            for (int pass = 0; pass < PASSES; pass++) {
                boolean moved = false;
                for (Set<String> group : movable) {
                    double[] shift = shiftOutOf(output, group, center, fixedWidth, fixedHeight);
                    if (shift != null) {
                        move(output, group, shift);
                        pushed.add(group);
                        moved = true;
                    }
                }
                if (!moved) {
                    break;
                }
            }
        }
        separate(output, fixedIds, movable, pushed);
        return output;
    }

    private static List<Set<String>> movableGroups(Map<String, Point> positions, Set<String> fixedIds,
                                                   Collection<Set<String>> groups) {
        List<Set<String>> movable = new ArrayList<>();
        Set<String> grouped = new HashSet<>();
        for (Set<String> group : groups) {
            grouped.addAll(group);
            movable.add(group);
        }
        for (String id : positions.keySet()) {
            if (!grouped.contains(id) && !fixedIds.contains(id)) {
                movable.add(Set.of(id));
            }
        }
        return movable;
    }

    /** The smallest straight move that takes every card of the group out of the zone, or null if none is inside. */
    private static double[] shiftOutOf(Map<String, Point> positions, Set<String> group, Point center,
                                       double zoneWidth, double zoneHeight) {
        double right = 0;
        double left = 0;
        double down = 0;
        double up = 0;
        boolean inside = false;
        for (String id : group) {
            Point p = positions.get(id);
            if (Math.abs(p.x() - center.x()) < zoneWidth && Math.abs(p.y() - center.y()) < zoneHeight) {
                inside = true;
                right = Math.max(right, center.x() + zoneWidth - p.x());
                left = Math.max(left, p.x() - (center.x() - zoneWidth));
                down = Math.max(down, center.y() + zoneHeight - p.y());
                up = Math.max(up, p.y() - (center.y() - zoneHeight));
            }
        }
        if (!inside) {
            return null;
        }
        double smallest = Math.min(right, Math.min(left, Math.min(down, up)));
        if (smallest == down) {
            return new double[]{0, down};
        }
        if (smallest == up) {
            return new double[]{0, -up};
        }
        return smallest == right ? new double[]{right, 0} : new double[]{-left, 0};
    }

    /** A pushed group gives way; whatever it touches gives way too. */
    private static void separate(Map<String, Point> positions, Set<String> fixedIds,
                                 List<Set<String>> movable, Set<Set<String>> pushed) {
        double gapWidth = Node.WIDTH + CARD_GAP;
        double gapHeight = Node.HEIGHT + CARD_GAP;
        for (int pass = 0; pass < PASSES; pass++) {
            boolean moved = false;
            for (Set<String> group : new ArrayList<>(pushed)) {
                for (Set<String> other : movable) {
                    if (other == group) {
                        continue;
                    }
                    for (String id : other) {
                        double[] shift = shiftOutOf(positions, group, positions.get(id), gapWidth, gapHeight);
                        if (shift != null) {
                            move(positions, group, shift);
                            pushed.add(other);
                            moved = true;
                        }
                    }
                }
                for (String id : fixedIds) {
                    double[] shift = shiftOutOf(positions, group, positions.get(id), gapWidth, gapHeight);
                    if (shift != null) {
                        move(positions, group, shift);
                        moved = true;
                    }
                }
            }
            if (!moved) {
                return;
            }
        }
    }

    private static void move(Map<String, Point> positions, Set<String> group, double[] shift) {
        for (String id : group) {
            Point p = positions.get(id);
            positions.put(id, new Point(p.x() + shift[0], p.y() + shift[1]));
        }
    }
}

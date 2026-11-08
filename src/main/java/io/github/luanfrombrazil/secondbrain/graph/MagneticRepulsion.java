package io.github.luanfrombrazil.secondbrain.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Moves nearby cards out from under fixed cards and separates whatever touches as a result. */
final class MagneticRepulsion {

    private static final int PASSES = 40;
    private static final double CARD_GAP = 16;

    private MagneticRepulsion() {
    }

    static Map<String, Point> apply(Map<String, Point> positions, Set<String> fixedIds, double radius) {
        if (fixedIds.isEmpty()) {
            return positions;
        }
        Map<String, Point> output = new HashMap<>(positions);
        Set<String> pushed = new HashSet<>();
        double halfWidth = Node.WIDTH + radius;
        double halfHeight = Node.HEIGHT + radius;

        for (String fixed : fixedIds) {
            Point center = output.get(fixed);
            for (var entry : positions.entrySet()) {
                String id = entry.getKey();
                if (fixedIds.contains(id)) {
                    continue;
                }
                Point current = output.get(id);
                double dx = current.x() - center.x();
                double dy = current.y() - center.y();
                if (Math.abs(dx) >= halfWidth || Math.abs(dy) >= halfHeight) {
                    continue;
                }
                if (dx == 0 && dy == 0) {
                    dx = 1;
                }
                double t = Math.min(
                        dx == 0 ? Double.MAX_VALUE : halfWidth / Math.abs(dx),
                        dy == 0 ? Double.MAX_VALUE : halfHeight / Math.abs(dy));
                output.put(id, new Point(center.x() + dx * t, center.y() + dy * t));
                pushed.add(id);
            }
        }
        separate(output, fixedIds, pushed);
        return output;
    }

    /** The pushed card gives way; whatever it touches gives way too. */
    private static void separate(Map<String, Point> pos, Set<String> fixedIds, Set<String> pushed) {
        double minX = Node.WIDTH + CARD_GAP;
        double minY = Node.HEIGHT + CARD_GAP;
        for (int pass = 0; pass < PASSES; pass++) {
            boolean moved = false;
            for (String a : List.copyOf(pushed)) {
                for (String b : new ArrayList<>(pos.keySet())) {
                    if (a.equals(b)) {
                        continue;
                    }
                    Point pa = pos.get(a);
                    Point pb = pos.get(b);
                    double dx = pa.x() - pb.x();
                    double dy = pa.y() - pb.y();
                    double overlapX = minX - Math.abs(dx);
                    double overlapY = minY - Math.abs(dy);
                    if (overlapX <= 0 || overlapY <= 0) {
                        continue;
                    }
                    if (overlapY < overlapX) {
                        double sign = dy >= 0 ? 1 : -1;
                        pos.put(a, new Point(pa.x(), pa.y() + sign * overlapY));
                    } else {
                        double sign = dx >= 0 ? 1 : -1;
                        pos.put(a, new Point(pa.x() + sign * overlapX, pa.y()));
                    }
                    if (!fixedIds.contains(b)) {
                        pushed.add(b);
                    }
                    moved = true;
                }
            }
            if (!moved) {
                return;
            }
        }
    }
}

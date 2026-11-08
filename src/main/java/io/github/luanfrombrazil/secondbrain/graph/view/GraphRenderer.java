package io.github.luanfrombrazil.secondbrain.graph.view;

import io.github.luanfrombrazil.secondbrain.graph.Edge;
import io.github.luanfrombrazil.secondbrain.graph.Node;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Draws the graph in screen coordinates, with one card drawing per zoom level. */
final class GraphRenderer {

    static final Color COLOR_BLOCKED = Color.web("#e0524d");
    static final Color COLOR_AVAILABLE = Color.web("#e7b53c");
    static final Color COLOR_IN_PROGRESS = Color.web("#4f8fea");
    static final Color COLOR_DONE = Color.web("#47b26b");

    private static final Color COLOR_SPACE = Color.web("#17181c");
    private static final Color COLOR_FOLDER = Color.web("#20232b");
    private static final Color COLOR_CARD = Color.web("#2a2c33");
    private static final Color COLOR_BORDER = Color.web("#3c3f48");
    private static final Color COLOR_SELECTION = Color.web("#f0f1f4");
    private static final Color COLOR_TEXT = Color.web("#ecedf0");
    private static final Color COLOR_TEXT_DIM = Color.web("#9ea3ad");
    private static final Color COLOR_CONNECTION = Color.web("#7d828e");
    private static final Color COLOR_HIGHLIGHT = Color.web("#d7dae1");
    private static final Color COLOR_LINK = Color.web("#8f7fd1");
    private static final Color COLOR_TRACK = Color.web("#454852");
    private static final Color COLOR_PROGRESS = Color.web("#8bb3f0");
    private static final Color COLOR_SHADOW = Color.web("#ffffff", 0.05);
    private static final double NOTE_WIDTH = 150;
    private static final double NOTE_HEIGHT = 40;

    private static final double HANDLE_HIT_RADIUS = 12;

    private final Map<Integer, Font> fonts =new HashMap<>();
    private final Map<Integer, Font> boldFonts = new HashMap<>();

    int draw(GraphView t, GraphicsContext g, Camera c, double width, double height) {
        g.setGlobalAlpha(1);
        g.setFill(COLOR_SPACE);
        g.fillRect(0, 0, width, height);
        if (t.groupByFolder) {
            drawFolders(t, g, c);
        }
        t.backdrop.draw(g, c, width, height);

        if (t.showConnections) {
            drawConnections(t, g, c, width, height);
        }
        if (t.level == ZoomLevel.NEAR) {
            if (t.showLinks) {
                drawLinks(t, g, c);
            }
            drawPointNotes(t, g, c, width, height);
        }

        int drawn = 0;
        for (CardData card : t.cards.values()) {
            GraphView.Visual v = t.visuals.get(card.id());
            if (v == null || offScreen(c, v, width, height)) {
                continue;
            }
            drawn++;
            if (t.levelTransition < 1) {
                g.setGlobalAlpha(1 - t.levelTransition);
                drawCard(t, g, c, card, v, t.previousLevel);
                g.setGlobalAlpha(t.levelTransition);
            }
            drawCard(t, g, c, card, v, t.level);
            g.setGlobalAlpha(1);
        }
        drawHandles(t, g, c);
        drawConnectionInProgress(t, g, c);
        drawTooltip(t, g, c);
        return drawn;
    }

    /** The card whose connection handle is at the screen point, or null. */
    String handleAt(GraphView t, Camera c, double screenX, double screenY) {
        if (t.level.compareTo(ZoomLevel.FAR) < 0) {
            return null;
        }
        for (CardData card : t.cards.values()) {
            GraphView.Visual v = t.visuals.get(card.id());
            if (v != null && Math.hypot(c.screenX(v.x + Node.WIDTH / 2) - screenX, c.screenY(v.y) - screenY) <= HANDLE_HIT_RADIUS) {
                return card.id();
            }
        }
        return null;
    }

    private void drawHandles(GraphView t, GraphicsContext g, Camera c) {
        if (t.level.compareTo(ZoomLevel.FAR) < 0) {
            return;
        }
        double radius = Math.max(5, 7 * c.scale());
        for (String id : java.util.Arrays.asList(t.hovered, t.selected, t.connectingFrom)) {
            GraphView.Visual v = id == null ? null : t.visuals.get(id);
            if (v != null) {
                double x = c.screenX(v.x + Node.WIDTH / 2);
                double y = c.screenY(v.y);
                g.setFill(COLOR_PROGRESS);
                g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
                g.setStroke(COLOR_SPACE);
                g.setLineWidth(1.5);
                g.strokeOval(x - radius, y - radius, 2 * radius, 2 * radius);
            }
        }
    }

    private void drawConnectionInProgress(GraphView t, GraphicsContext g, Camera c) {
        GraphView.Visual from = t.connectingFrom == null ? null : t.visuals.get(t.connectingFrom);
        if (from == null) {
            return;
        }
        boolean invalid = t.dropTarget != null && !t.graph.canConnect(t.connectingFrom, t.dropTarget);
        Color color = invalid ? COLOR_BLOCKED : COLOR_HIGHLIGHT;
        double x1 = c.screenX(from.x + Node.WIDTH / 2);
        double y1 = c.screenY(from.y);
        g.setStroke(color);
        g.setFill(color);
        g.setLineWidth(2);
        g.strokeLine(x1, y1, t.mouseX, t.mouseY);
        drawArrowhead(g, x1, y1, t.mouseX, t.mouseY, 12);

        GraphView.Visual to = t.dropTarget == null ? null : t.visuals.get(t.dropTarget);
        if (to != null) {
            double w = Node.WIDTH * c.scale();
            double h = Node.HEIGHT * c.scale();
            double corner = Math.max(3, 16 * c.scale());
            g.setLineWidth(2.5);
            g.strokeRoundRect(c.screenX(to.x) - w / 2, c.screenY(to.y) - h / 2, w, h, corner, corner);
        }
    }

    /** The card under the screen point, or null. */
    String cardAt(GraphView t, Camera c, double screenX, double screenY) {
        double worldX = c.worldX(screenX);
        double worldY = c.worldY(screenY);
        String found = null;
        for (CardData card : t.cards.values()) {
            GraphView.Visual v = t.visuals.get(card.id());
            if (v == null) {
                continue;
            }
            if (t.level == ZoomLevel.FARTHEST) {
                double radius = dotRadius(card) + 3;
                if (Math.hypot(c.screenX(v.x) - screenX, c.screenY(v.y) - screenY) <= radius) {
                    found = card.id();
                }
            } else if (Math.abs(worldX - v.x) <= Node.WIDTH / 2 && Math.abs(worldY - v.y) <= Node.HEIGHT / 2) {
                found = card.id();
            }
        }
        return found;
    }

    private boolean offScreen(Camera c, GraphView.Visual v, double width, double height) {
        double gap = Math.max(20, Node.WIDTH * c.scale());
        double x = c.screenX(v.x);
        double y = c.screenY(v.y);
        return x < -gap || y < -gap || x > width + gap || y > height + gap;
    }

    // Folders: convex hull with rounded corners, solid color behind the grid.

    private void drawFolders(GraphView t, GraphicsContext g, Camera c) {
        double rounding = 70 * c.scale();
        List<Runnable> names = new ArrayList<>();
        for (var folder : t.folders().entrySet()) {
            List<double[]> points = new ArrayList<>();
            for (String id : folder.getValue()) {
                GraphView.Visual v = t.visuals.get(id);
                if (v == null) {
                    continue;
                }
                for (int sx = -1; sx <= 1; sx += 2) {
                    for (int sy = -1; sy <= 1; sy += 2) {
                        points.add(new double[]{
                                c.screenX(v.x + sx * Node.WIDTH / 2),
                                c.screenY(v.y + sy * Node.HEIGHT / 2)});
                    }
                }
            }
            if (points.size() < 4) {
                continue;
            }
            List<double[]> hull = convexHull(points);
            double[] xs = hull.stream().mapToDouble(p -> p[0]).toArray();
            double[] ys = hull.stream().mapToDouble(p -> p[1]).toArray();
            g.setFill(COLOR_FOLDER);
            g.setStroke(COLOR_FOLDER);
            g.setLineWidth(rounding);
            g.setLineJoin(StrokeLineJoin.ROUND);
            g.fillPolygon(xs, ys, xs.length);
            g.strokePolygon(xs, ys, xs.length);
            if (t.level.compareTo(ZoomLevel.VERY_FAR) >= 0) {
                double top = hull.stream().mapToDouble(p -> p[1]).min().orElse(0);
                double left = hull.stream().filter(p -> p[1] < top + 2)
                        .mapToDouble(p -> p[0]).min().orElse(0);
                String name = folder.getKey();
                names.add(() -> g.fillText(name, left, top - rounding / 2 - 2));
            }
        }
        g.setLineJoin(StrokeLineJoin.MITER);
        // After all areas, so one folder does not cover the name of another.
        g.setFill(COLOR_TEXT_DIM);
        g.setFont(fontFor(Math.clamp(26 * c.scale(), 11, 22), false));
        g.setTextBaseline(VPos.BOTTOM);
        names.forEach(Runnable::run);
    }

    /** Andrew's monotone chain. */
    private static List<double[]> convexHull(List<double[]> points) {
        List<double[]> sorted = new ArrayList<>(points);
        sorted.sort(Comparator.<double[]>comparingDouble(p -> p[0]).thenComparingDouble(p -> p[1]));
        List<double[]> hull = new ArrayList<>();
        for (int pass = 0; pass < 2; pass++) {
            int start = hull.size();
            for (double[] p : sorted) {
                while (hull.size() >= start + 2
                        && cross(hull.get(hull.size() - 2), hull.getLast(), p) <= 0) {
                    hull.removeLast();
                }
                hull.add(p);
            }
            hull.removeLast();
            java.util.Collections.reverse(sorted);
        }
        return hull;
    }

    private static double cross(double[] o, double[] a, double[] b) {
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]);
    }

    // Arrows: curved when the parent has several children or the child has several parents; otherwise straight.

    private void drawConnections(GraphView t, GraphicsContext g, Camera c, double width, double height) {
        boolean dots = t.level == ZoomLevel.FARTHEST;
        String focus = t.hovered != null ? t.hovered : t.selected;
        double thickness = Math.clamp(2.2 * c.scale(), 1, 2.6);
        for (Edge a : t.connections()) {
            boolean highlight = focus != null && (a.source().equals(focus) || a.target().equals(focus));
            if (dots && !highlight) {
                continue;
            }
            GraphView.Visual o = t.visuals.get(a.source());
            GraphView.Visual d = t.visuals.get(a.target());
            if (o == null || d == null || segmentOffScreen(c, o, d, width, height)) {
                continue;
            }
            Color color = highlight ? COLOR_HIGHLIGHT : COLOR_CONNECTION;
            g.setStroke(color);
            g.setFill(color);
            g.setLineWidth(highlight ? thickness + 0.8 : thickness);
            drawArrow(t, g, c, a, o, d, dots);
        }
    }

    private boolean segmentOffScreen(Camera c, GraphView.Visual o, GraphView.Visual d, double width, double height) {
        double gap = Node.WIDTH * c.scale();
        double ax = c.screenX(o.x);
        double bx = c.screenX(d.x);
        double ay = c.screenY(o.y);
        double by = c.screenY(d.y);
        return Math.max(ax, bx) < -gap || Math.min(ax, bx) > width + gap
                || Math.max(ay, by) < -gap || Math.min(ay, by) > height + gap;
    }

    private void drawArrow(GraphView t, GraphicsContext g, Camera c, Edge a,
                              GraphView.Visual o, GraphView.Visual d, boolean fromCenters) {
        List<String> siblings = t.graph.dependentsOf(a.source());
        List<String> parents = t.graph.dependenciesOf(a.target());
        boolean curved = siblings.size() >= 2 || parents.size() >= 2;
        double sx;
        double sy;
        double ex;
        double ey;
        boolean horizontal = t.type == LayoutType.DEPENDENCY && d.x > o.x;
        if (fromCenters) {
            sx = o.x;
            sy = o.y;
            ex = d.x;
            ey = d.y;
        } else if (horizontal) {
            sx = o.x + Node.WIDTH / 2;
            sy = o.y;
            ex = d.x - Node.WIDTH / 2;
            ey = d.y;
        } else {
            double[] output = onBorder(o.x, o.y, d.x, d.y);
            double[] arrival = onBorder(d.x, d.y, o.x, o.y);
            sx = output[0];
            sy = output[1];
            ex = arrival[0];
            ey = arrival[1];
        }

        double x1 = c.screenX(sx);
        double y1 = c.screenY(sy);
        double x2 = c.screenX(ex);
        double y2 = c.screenY(ey);
        double c2x = x1;
        double c2y = y1;
        g.beginPath();
        g.moveTo(x1, y1);
        if (!curved) {
            g.lineTo(x2, y2);
        } else if (horizontal) {
            double gap = Math.max(30 * c.scale(), (x2 - x1) / 2);
            c2x = x2 - gap;
            c2y = y2;
            g.bezierCurveTo(x1 + gap, y1, c2x, c2y, x2, y2);
        } else {
            double length = Math.max(1, Math.hypot(x2 - x1, y2 - y1));
            double nx = -(y2 - y1) / length;
            double ny = (x2 - x1) / length;
            boolean fansOut = siblings.size() >= 2;
            int index = fansOut ? siblings.indexOf(a.target()) : parents.indexOf(a.source());
            double arc = length * 0.16 * (index * 2 < (fansOut ? siblings : parents).size() ? 1 : -1);
            c2x = x1 + (x2 - x1) * 0.67 + nx * arc;
            c2y = y1 + (y2 - y1) * 0.67 + ny * arc;
            g.bezierCurveTo(x1 + (x2 - x1) * 0.33 + nx * arc, y1 + (y2 - y1) * 0.33 + ny * arc,
                    c2x, c2y, x2, y2);
        }
        g.stroke();
        if (!fromCenters) {
            drawArrowhead(g, curved ? c2x : x1, curved ? c2y : y1, x2, y2, Math.clamp(11 * c.scale(), 5, 13));
        }
    }

    private static void drawArrowhead(GraphicsContext g, double fromX, double fromY, double x, double y, double size) {
        double angle = Math.atan2(y - fromY, x - fromX);
        double spread = 0.45;
        g.fillPolygon(
                new double[]{x, x - size * Math.cos(angle - spread), x - size * Math.cos(angle + spread)},
                new double[]{y, y - size * Math.sin(angle - spread), y - size * Math.sin(angle + spread)},
                3);
    }

    /** Where the line from the center (ax, ay) toward (bx, by) crosses the card border. */
    private static double[] onBorder(double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        if (dx == 0 && dy == 0) {
            return new double[]{ax, ay};
        }
        double t = Math.min(
                dx == 0 ? Double.MAX_VALUE : Node.WIDTH / 2 / Math.abs(dx),
                dy == 0 ? Double.MAX_VALUE : Node.HEIGHT / 2 / Math.abs(dy));
        return new double[]{ax + dx * t, ay + dy * t};
    }

    private void drawLinks(GraphView t, GraphicsContext g, Camera c) {
        g.setStroke(COLOR_LINK);
        g.setLineWidth(1.4);
        g.setLineDashes(6, 6);
        for (Edge a : t.graph.links()) {
            GraphView.Visual o = t.visuals.get(a.source());
            GraphView.Visual d = t.visuals.get(a.target());
            if (o != null && d != null) {
                g.strokeLine(c.screenX(o.x), c.screenY(o.y), c.screenX(d.x), c.screenY(d.y));
            }
        }
        g.setLineDashes((double[]) null);
    }

    /** The point notes of the focused card: the selected one, or the one closest to the screen center. */
    private void drawPointNotes(GraphView t, GraphicsContext g, Camera c, double width, double height) {
        String focus = t.selected;
        if (focus == null) {
            double best = Double.MAX_VALUE;
            for (var e : t.visuals.entrySet()) {
                double d = Math.hypot(c.screenX(e.getValue().x) - width / 2, c.screenY(e.getValue().y) - height / 2);
                if (d < best) {
                    best = d;
                    focus = e.getKey();
                }
            }
        }
        GraphView.Visual v = focus == null ? null : t.visuals.get(focus);
        if (v == null) {
            return;
        }
        List<PointNote> notes = new ArrayList<>();
        for (PointNote note : t.pointNotes) {
            if (note.linkedTo().contains(focus)) {
                notes.add(note);
            }
        }
        double step = NOTE_WIDTH + 14;
        double start = v.x - step * (notes.size() - 1) / 2;
        double y = v.y - Node.HEIGHT / 2 - 62;
        for (int i = 0; i < notes.size(); i++) {
            double x = start + i * step;
            g.setStroke(COLOR_LINK);
            g.setLineWidth(1.2);
            g.setLineDashes(5, 5);
            g.strokeLine(c.screenX(x), c.screenY(y + NOTE_HEIGHT / 2), c.screenX(v.x), c.screenY(v.y - Node.HEIGHT / 2));
            g.setLineDashes((double[]) null);
            double px = c.screenX(x - NOTE_WIDTH / 2);
            double py = c.screenY(y - NOTE_HEIGHT / 2);
            double w = NOTE_WIDTH * c.scale();
            double h = NOTE_HEIGHT * c.scale();
            g.setFill(COLOR_CARD);
            g.fillRoundRect(px, py, w, h, 8, 8);
            g.setStroke(COLOR_LINK);
            g.strokeRoundRect(px, py, w, h, 8, 8);
            text(g, notes.get(i).title(), px + 8 * c.scale(), py + h / 2, w - 16 * c.scale(),
                    12 * c.scale(), false, COLOR_TEXT, VPos.CENTER);
        }
    }

    // Cards

    private double dotRadius(CardData card) {
        return 3 + 9 * card.weight();
    }

    private void drawCard(GraphView t, GraphicsContext g, Camera c, CardData card,
                              GraphView.Visual v, ZoomLevel renderLevel) {
        double cx = c.screenX(v.x);
        double cy = c.screenY(v.y);
        boolean selected = card.id().equals(t.selected);
        if (renderLevel == ZoomLevel.FARTHEST) {
            double r = dotRadius(card);
            g.setFill(card.stateColor());
            g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
            if (selected) {
                g.setStroke(COLOR_SELECTION);
                g.setLineWidth(1.5);
                g.strokeOval(cx - r - 2, cy - r - 2, 2 * r + 4, 2 * r + 4);
            }
            return;
        }

        double e = c.scale();
        double w = Node.WIDTH * e;
        double h = Node.HEIGHT * e;
        double x = cx - w / 2;
        double y = cy - h / 2;
        double corner = Math.max(3, 16 * e);
        g.setFill(COLOR_SHADOW);
        g.fillRoundRect(x - 2, y - 2, w + 4, h + 4, corner + 3, corner + 3);
        g.setFill(COLOR_CARD);
        g.fillRoundRect(x, y, w, h, corner, corner);

        // The state stripe follows the curve of the top corners.
        double strip = Math.max(2, 8 * e);
        g.save();
        g.beginPath();
        g.rect(x, y, w, strip);
        g.clip();
        g.setFill(card.stateColor());
        g.fillRoundRect(x, y, w, h, corner, corner);
        g.restore();

        g.setStroke(selected ? COLOR_SELECTION : COLOR_BORDER);
        g.setLineWidth(selected ? 2 : 1);
        g.strokeRoundRect(x, y, w, h, corner, corner);

        double margin = 14 * e;
        double usableWidth = w - 2 * margin;
        double barY = y + h - 14 * e;
        drawBar(g, x + margin, barY, usableWidth, Math.max(2, 5 * e), card.progress());

        switch (renderLevel) {
            case FAR -> {
                g.setFill(COLOR_TEXT_DIM);
                g.fillRect(x + margin, y + 26 * e, usableWidth * 0.75, 9 * e);
                g.fillRect(x + margin, y + 44 * e, usableWidth * 0.5, 7 * e);
            }
            case MEDIUM -> text(g, card.title(), x + margin, y + 42 * e, usableWidth,
                    21 * e, true, COLOR_TEXT, VPos.CENTER);
            case NEAR -> drawFullCard(g, card, x, y, w, margin, e);
            default -> {
            }
        }
    }

    private void drawFullCard(GraphicsContext g, CardData card, double x, double y, double w, double margin, double e) {
        text(g, card.title(), x + margin, y + 28 * e, w - 2 * margin - 22 * e, 15 * e, true, COLOR_TEXT, VPos.CENTER);
        if (card.hasInnerGraph()) {
            g.setStroke(COLOR_TEXT_DIM);
            g.setLineWidth(1.2);
            g.strokeRect(x + w - margin - 12 * e, y + 22 * e, 12 * e, 12 * e);
        }
        double line = y + 50 * e;
        int done = card.tasksDone();
        List<String> tasks = card.tasks();
        for (int i = 0; i < Math.min(2, tasks.size()); i++) {
            double side = 9 * e;
            g.setStroke(COLOR_TEXT_DIM);
            g.setLineWidth(1);
            g.strokeRect(x + margin, line + i * 17 * e - side / 2, side, side);
            if (i < done) {
                g.setFill(COLOR_PROGRESS);
                g.fillRect(x + margin + 2 * e, line + i * 17 * e - side / 2 + 2 * e, side - 4 * e, side - 4 * e);
            }
            text(g, tasks.get(i), x + margin + 15 * e, line + i * 17 * e, w - 2 * margin - 15 * e,
                    11.5 * e, false, COLOR_TEXT_DIM, VPos.CENTER);
        }
        if (!tasks.isEmpty()) {
            text(g, done + "/" + tasks.size(), x + w - margin - 28 * e, y + 82 * e, 30 * e,
                    10.5 * e, false, COLOR_TEXT_DIM, VPos.CENTER);
        }
    }

    private void drawBar(GraphicsContext g, double x, double y, double width, double height, double value) {
        g.setFill(COLOR_TRACK);
        g.fillRoundRect(x, y, width, height, height, height);
        if (value > 0) {
            g.setFill(COLOR_PROGRESS);
            g.fillRoundRect(x, y, width * Math.min(1, value), height, height, height);
        }
    }

    private void drawTooltip(GraphView t, GraphicsContext g, Camera c) {
        if (t.hovered == null || t.level.compareTo(ZoomLevel.FAR) > 0) {
            return;
        }
        CardData card = t.cards.get(t.hovered);
        GraphView.Visual v = t.visuals.get(t.hovered);
        if (card == null || v == null) {
            return;
        }
        String phrase = card.title() + "  ·  " + Math.round(card.progress() * 100) + "%";
        double width = phrase.length() * 6.9 + 18;
        double x = c.screenX(v.x) - width / 2;
        double lift = t.level == ZoomLevel.FARTHEST ? 12 : Node.HEIGHT * c.scale() / 2;
        double y = c.screenY(v.y) - lift - 34;
        g.setFill(Color.web("#0f1013"));
        g.fillRoundRect(x, y, width, 26, 8, 8);
        g.setStroke(COLOR_BORDER);
        g.setLineWidth(1);
        g.strokeRoundRect(x, y, width, 26, 8, 8);
        text(g, phrase, x + 9, y + 13, width, 12, false, COLOR_TEXT, VPos.CENTER);
    }

    private void text(GraphicsContext g, String phrase, double x, double y, double width, double size,
                       boolean bold, Color color, VPos base) {
        int fits = (int) (width / (size * (bold ? 0.58 : 0.54)));
        String output = phrase.length() <= fits ? phrase : phrase.substring(0, Math.max(0, fits - 1)) + "…";
        g.setFill(color);
        g.setFont(fontFor(size, bold));
        g.setTextBaseline(base);
        g.fillText(output, x, y);
    }

    private Font fontFor(double size, boolean bold) {
        int key = (int) Math.round(size * 2);
        return (bold ? boldFonts : fonts).computeIfAbsent(key, k -> Font.font(
                "Segoe UI", bold ? FontWeight.SEMI_BOLD : FontWeight.NORMAL, k / 2.0));
    }
}

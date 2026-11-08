package io.github.luanfrombrazil.secondbrain.graph.view;

import io.github.luanfrombrazil.secondbrain.graph.Edge;
import io.github.luanfrombrazil.secondbrain.graph.Graph;
import io.github.luanfrombrazil.secondbrain.graph.Node;
import io.github.luanfrombrazil.secondbrain.graph.Parameters;
import io.github.luanfrombrazil.secondbrain.graph.Point;
import io.github.luanfrombrazil.secondbrain.graph.ForceSimulation;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The infinite canvas. Handles camera, animation and mouse; drawing is done by {@link GraphRenderer}.
 * Everything goes on one Canvas, in screen coordinates, so text stays sharp at any zoom.
 */
public final class GraphView extends Region {

    private static final double CARD_SPEED = 7;
    private static final double LEVEL_TRANSITION_SECONDS = 0.18;
    private static final double MIN_DRAG = 4;
    private static final double FOCUS_SECONDS = 0.3;
    private static final int MAX_CARDS_TO_FADE = 400;

    /** Where the card is and where it is going. */
    static final class Visual {
        double x;
        double y;
        double targetX;
        double targetY;

        Visual(Point p) {
            x = targetX = p.x();
            y = targetY = p.y();
        }
    }

    private final Canvas canvas = new Canvas();
    private final Camera camera = new Camera();
    private final Parameters parameters;
    private final GraphRenderer renderer = new GraphRenderer();

    Graph graph = new Graph(List.of(), List.of(), List.of());
    final Map<String, CardData> cards = new LinkedHashMap<>();
    final Map<String, Visual> visuals = new HashMap<>();
    List<PointNote> pointNotes = List.of();

    LayoutType type = LayoutType.DEPENDENCY;
    Backdrop backdrop = Backdrop.GRID;
    boolean showConnections = true;
    boolean showLinks = true;
    boolean groupByFolder = true;
    ZoomLevel level = ZoomLevel.MEDIUM;
    ZoomLevel previousLevel = ZoomLevel.MEDIUM;
    double levelTransition = 1;
    String selected;
    String hovered;
    String focus;
    double focusProgress;
    private double focusAnchorX = Double.NaN;
    private double focusAnchorY = Double.NaN;
    private String followed;
    String connectingFrom;
    String dropTarget;
    double mouseX;
    double mouseY;

    private ForceSimulation simulation;
    private boolean dirty = true;
    private long previousFrame;
    private double pressX;
    private double pressY;
    private double lastX;
    private double lastY;
    private boolean dragged;
    private int looseCount;
    private double fps;
    private int drawn;

    private final AnimationTimer loop = new AnimationTimer() {
        @Override
        public void handle(long now) {
            double seconds = previousFrame == 0 ? 0.016 : Math.min(0.05, (now - previousFrame) / 1e9);
            previousFrame = now;
            advance(seconds);
            if (dirty) {
                drawNow();
                fps += (1 / Math.max(0.001, seconds) - fps) * 0.1;
            }
        }
    };

    public GraphView(Parameters parameters) {
        this.parameters = parameters;
        getChildren().add(canvas);
        setMinSize(0, 0);
        canvas.addEventHandler(MouseEvent.ANY, this::onMouse);
        canvas.setOnScroll(e -> {
            camera.zoomAtCursor(e.getX(), e.getY(), Math.pow(1.0018, e.getDeltaY()));
            focusAnchorX = e.getX();
            focusAnchorY = e.getY();
            followed = null;
            dirty = true;
        });
        loop.start();
    }

    @Override
    protected void layoutChildren() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        dirty = true;
    }

    public void show(Graph newGraph, Map<String, CardData> newCards, List<PointNote> notes) {
        graph = newGraph;
        cards.clear();
        cards.putAll(newCards);
        pointNotes = notes;
        visuals.clear();
        selected = null;
        hovered = null;
        focus = null;
        focusProgress = 0;
        followed = null;
        simulation = null;
        Map<String, Point> positions = type.layout().compute(graph, parameters);
        positions.forEach((id, p) -> visuals.put(id, new Visual(p)));
        frameTargets();
        camera.jumpToTarget();
        dirty = true;
    }

    /** Changing or forcing the layout sends loose cards to the strip (D-58) and recenters the camera. */
    public void reorganize(LayoutType next) {
        type = next;
        graph = graph.withoutFixedPoints();
        recompute();
        frameTargets();
    }

    /** A parameter changed: arranges again without touching the camera or loose cards. */
    public void recompute() {
        if (type == LayoutType.FORCES) {
            simulation = new ForceSimulation(graph, parameters, currentPositions());
        } else {
            simulation = null;
            setTargets(type.layout().compute(graph, parameters));
        }
        dirty = true;
    }

    /** Creates a loose card at a point in space: it stays there and its neighbours move away. */
    public void createLooseCard(double worldX, double worldY) {
        looseCount++;
        String id = "loose-" + looseCount;
        Point point = new Point(worldX, worldY);
        graph = graph.withNode(new Node(id, null, point));
        cards.put(id, new CardData(id, "Card solto " + looseCount, GraphRenderer.COLOR_AVAILABLE,
                0, 0.4, List.of(), 0, false));
        visuals.put(id, new Visual(point));
        selected = id;
        recompute();
    }

    /**
     * Connects two cards: the source becomes a prerequisite of the target. The camera follows
     * the target, so the card the user just dropped on does not jump away.
     */
    public void connect(String source, String target) {
        graph = graph.withConnection(source, target);
        followed = target;
        recompute();
    }

    /** A setting changed: arranges again with the camera locked on the card at the center of the screen. */
    public void recomputeFollowingCenter() {
        if (followed == null) {
            followed = closestTo(getWidth() / 2, getHeight() / 2);
        }
        recompute();
    }

    public void recenter() {
        frameTargets();
        dirty = true;
    }

    public void setBackdrop(Backdrop next) {
        backdrop = next;
        dirty = true;
    }

    public void setShowConnections(boolean value) {
        showConnections = value;
        dirty = true;
    }

    public void setShowLinks(boolean value) {
        showLinks = value;
        dirty = true;
    }

    public void setGroupByFolder(boolean value) {
        groupByFolder = value;
        dirty = true;
    }

    public void markDirty() {
        dirty = true;
    }

    public LayoutType layoutType() {
        return type;
    }

    public Camera camera() {
        return camera;
    }

    public ZoomLevel level() {
        return level;
    }

    public double fps() {
        return fps;
    }

    public int drawn() {
        return drawn;
    }

    public int totalCards() {
        return cards.size();
    }

    public boolean simulating() {
        return simulation != null;
    }

    /** Finishes animations and simulation at once. Used for screenshots. */
    public void settle() {
        if (simulation != null) {
            while (simulation.step()) {
                // until it settles
            }
            apply(simulation.positions());
            simulation = null;
        }
        visuals.values().forEach(v -> {
            v.x = v.targetX;
            v.y = v.targetY;
        });
        camera.jumpToTarget();
        updateLevel();
        levelTransition = 1;
        updateFocus(FOCUS_SECONDS);
        updateFocus(FOCUS_SECONDS);
    }

    public void drawNow() {
        drawn = renderer.draw(this, canvas.getGraphicsContext2D(), camera, canvas.getWidth(), canvas.getHeight());
        dirty = false;
    }

    public void stopAnimation() {
        loop.stop();
    }

    public Canvas canvas() {
        return canvas;
    }

    public void frameImmediately() {
        frameTargets();
        camera.jumpToTarget();
    }

    private void advance(double seconds) {
        if (camera.update(seconds)) {
            dirty = true;
        }
        Visual tracked = followed == null ? null : visuals.get(followed);
        double trackedX = tracked == null ? 0 : tracked.x;
        double trackedY = tracked == null ? 0 : tracked.y;
        moveCards(seconds);
        if (tracked != null && (tracked.x != trackedX || tracked.y != trackedY)) {
            camera.drag((trackedX - tracked.x) * camera.scale(), (trackedY - tracked.y) * camera.scale());
        }
        updateLevel();
        if (levelTransition < 1) {
            levelTransition = Math.min(1, levelTransition + seconds / LEVEL_TRANSITION_SECONDS);
            dirty = true;
        }
        updateFocus(seconds);
    }

    private void moveCards(double seconds) {
        if (simulation != null) {
            boolean keepGoing = simulation.step();
            apply(simulation.positions());
            visuals.values().forEach(v -> {
                v.x = v.targetX;
                v.y = v.targetY;
            });
            dirty = true;
            if (!keepGoing) {
                simulation = null;
            }
        } else {
            double t = 1 - Math.exp(-seconds * CARD_SPEED);
            for (Visual v : visuals.values()) {
                double dx = v.targetX - v.x;
                double dy = v.targetY - v.y;
                if (dx != 0 || dy != 0) {
                    boolean arrived = Math.abs(dx) < 0.1 && Math.abs(dy) < 0.1;
                    v.x = arrived ? v.targetX : v.x + dx * t;
                    v.y = arrived ? v.targetY : v.y + dy * t;
                    dirty = true;
                }
            }
        }
    }

    /** Up close, the linked elements slide to the focused card; zooming out sends them back. */
    private void updateFocus(double seconds) {
        double change = seconds / FOCUS_SECONDS;
        if (level.compareTo(ZoomLevel.MEDIUM) >= 0) {
            String next = findFocus();
            if (!java.util.Objects.equals(next, focus)) {
                focus = next;
                focusProgress = 0;
                dirty = true;
            }
        }
        if (level == ZoomLevel.NEAR) {
            if (focusProgress < 1) {
                focusProgress = Math.min(1, focusProgress + change);
                dirty = true;
            }
        } else if (focusProgress > 0) {
            focusProgress = Math.max(0, focusProgress - change);
            dirty = true;
        }
    }

    /** The selected card if it is on screen; otherwise the card closest to where the zoom is aimed. */
    private String findFocus() {
        double width = getWidth();
        double height = getHeight();
        Visual chosen = selected == null ? null : visuals.get(selected);
        if (chosen != null) {
            double x = camera.screenX(chosen.x);
            double y = camera.screenY(chosen.y);
            if (x >= 0 && y >= 0 && x <= width && y <= height) {
                return selected;
            }
        }
        double anchorX = Double.isNaN(focusAnchorX) ? width / 2 : focusAnchorX;
        double anchorY = Double.isNaN(focusAnchorY) ? height / 2 : focusAnchorY;
        return closestTo(anchorX, anchorY);
    }

    private String closestTo(double anchorX, double anchorY) {
        String closest = null;
        double best = Double.MAX_VALUE;
        for (var entry : visuals.entrySet()) {
            double distance = Math.hypot(camera.screenX(entry.getValue().x) - anchorX,
                    camera.screenY(entry.getValue().y) - anchorY);
            if (distance < best) {
                best = distance;
                closest = entry.getKey();
            }
        }
        return closest;
    }

    private void updateLevel() {
        ZoomLevel next = ZoomLevel.forScale(camera.scale(), level, parameters.zoomThresholds);
        if (next != level) {
            previousLevel = level;
            level = next;
            // Fading draws every card twice, which is not worth it with this many on screen.
            levelTransition = drawn > MAX_CARDS_TO_FADE ? 1 : 0;
            dirty = true;
        }
    }

    private Map<String, Point> currentPositions() {
        Map<String, Point> positions = new HashMap<>();
        visuals.forEach((id, v) -> positions.put(id, new Point(v.x, v.y)));
        return positions;
    }

    private void apply(Map<String, Point> positions) {
        setTargets(positions);
    }

    private void setTargets(Map<String, Point> positions) {
        positions.forEach((id, p) -> {
            Visual v = visuals.computeIfAbsent(id, k -> new Visual(p));
            v.targetX = p.x();
            v.targetY = p.y();
        });
    }

    private void frameTargets() {
        if (visuals.isEmpty()) {
            camera.frame(-Node.WIDTH, -Node.HEIGHT, Node.WIDTH, Node.HEIGHT,
                    getWidth() > 0 ? getWidth() : 1200, getHeight() > 0 ? getHeight() : 800);
            return;
        }
        Map<String, Point> targets = new HashMap<>();
        if (type == LayoutType.FORCES && simulation != null) {
            targets.putAll(type.layout().compute(graph, parameters));
        } else {
            visuals.forEach((id, v) -> targets.put(id, new Point(v.targetX, v.targetY)));
        }
        double minX = targets.values().stream().mapToDouble(Point::x).min().orElse(0) - Node.WIDTH / 2;
        double maxX = targets.values().stream().mapToDouble(Point::x).max().orElse(0) + Node.WIDTH / 2;
        double minY = targets.values().stream().mapToDouble(Point::y).min().orElse(0) - Node.HEIGHT / 2;
        double maxY = targets.values().stream().mapToDouble(Point::y).max().orElse(0) + Node.HEIGHT / 2;
        double width = getWidth() > 0 ? getWidth() : 1200;
        double height = getHeight() > 0 ? getHeight() : 800;
        camera.frame(minX, minY, maxX, maxY, width, height);
    }

    private void onMouse(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
            pressX = lastX = e.getX();
            pressY = lastY = e.getY();
            dragged = false;
            connectingFrom = switch (e.getButton()) {
                case PRIMARY -> renderer.handleAt(this, camera, e.getX(), e.getY());
                case SECONDARY -> renderer.cardAt(this, camera, e.getX(), e.getY());
                default -> null;
            };
        } else if (e.getEventType() == MouseEvent.MOUSE_RELEASED) {
            if (connectingFrom != null) {
                if (dropTarget != null && graph.canConnect(connectingFrom, dropTarget)) {
                    connect(connectingFrom, dropTarget);
                }
                connectingFrom = null;
                dropTarget = null;
                dirty = true;
            }
        } else if (e.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            if (Math.hypot(e.getX() - pressX, e.getY() - pressY) > MIN_DRAG) {
                dragged = true;
            }
            if (connectingFrom != null) {
                String under = renderer.cardAt(this, camera, e.getX(), e.getY());
                dropTarget = connectingFrom.equals(under) ? null : under;
                dirty = true;
            } else if (dragged) {
                focusAnchorX = Double.NaN;
                focusAnchorY = Double.NaN;
                followed = null;
                camera.drag(e.getX() - lastX, e.getY() - lastY);
                dirty = true;
            }
            lastX = e.getX();
            lastY = e.getY();
        } else if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
            String handle = renderer.handleAt(this, camera, e.getX(), e.getY());
            String now = handle != null ? handle : renderer.cardAt(this, camera, e.getX(), e.getY());
            if (!java.util.Objects.equals(now, hovered)
                    || (now != null && level.compareTo(ZoomLevel.MEDIUM) < 0)) {
                hovered = now;
                dirty = true;
            }
        } else if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
            hovered = null;
            dirty = true;
        } else if (e.getEventType() == MouseEvent.MOUSE_CLICKED
                && e.getButton() == MouseButton.PRIMARY && !dragged) {
            String clicked = renderer.cardAt(this, camera, e.getX(), e.getY());
            if (e.getClickCount() == 2 && clicked == null) {
                createLooseCard(camera.worldX(e.getX()), camera.worldY(e.getY()));
            } else if (e.getClickCount() == 1) {
                selected = clicked;
                Visual visual = clicked == null ? null : visuals.get(clicked);
                if (visual != null) {
                    focusAnchorX = Double.NaN;
                    focusAnchorY = Double.NaN;
                    followed = clicked;
                    camera.centerOn(visual.targetX, visual.targetY, getWidth(), getHeight());
                }
            }
            dirty = true;
        }
    }

    /** The ids grouped by each folder. */
    Map<String, List<String>> folders() {
        Map<String, List<String>> output = new LinkedHashMap<>();
        for (Node node : graph.nodes()) {
            if (node.folder() != null) {
                output.computeIfAbsent(node.folder(), k -> new ArrayList<>()).add(node.id());
            }
        }
        return output;
    }

    List<Edge> connections() {
        return graph.connections();
    }
}

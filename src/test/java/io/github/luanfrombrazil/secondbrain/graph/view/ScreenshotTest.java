package io.github.luanfrombrazil.secondbrain.graph.view;

import io.github.luanfrombrazil.secondbrain.graph.Parameters;
import io.github.luanfrombrazil.secondbrain.graph.Node;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Generates screenshots. Only runs with: mvn test -Dcapture=true -Dtest=ScreenshotTest */
@EnabledIfSystemProperty(named = "capture", matches = "true")
class ScreenshotTest {

    private static final int WIDTH = 1500;
    private static final int HEIGHT = 900;

    @Test
    void capture() throws Exception {
        Path folder = Path.of("target", "captures");
        Files.createDirectories(folder);
        CountDownLatch end = new CountDownLatch(1);
        Platform.startup(() -> {
            try {
                generate(folder);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                end.countDown();
            }
        });
        assertTrue(end.await(120, TimeUnit.SECONDS));
        if (failure != null) {
            throw failure;
        }
    }

    private AssertionError failure;

    private void generate(Path folder) throws IOException {
        for (LayoutType type : LayoutType.values()) {
            capture(folder, type, 50, null, type.name().toLowerCase());
        }
        capture(folder, LayoutType.DEPENDENCY, 200, null, "dependency-200");
        capture(folder, LayoutType.DEPENDENCY, 10, 0.08, "zoom-farthest");
        capture(folder, LayoutType.DEPENDENCY, 10, 0.2, "zoom-very-far");
        capture(folder, LayoutType.DEPENDENCY, 10, 0.42, "zoom-far");
        capture(folder, LayoutType.DEPENDENCY, 10, 0.72, "zoom-medium");
        capture(folder, LayoutType.DEPENDENCY, 10, 1.0, "zoom-near");
        dragFromHandleCreatesConnection();
        captureLinkedElements(folder);
        benchmark();
    }

    /** Prints the milliseconds per frame, including the GPU work forced by the snapshot. */
    private void benchmark() {
        for (int size : new int[]{200, 2000}) {
            GraphView view = new GraphView(new Parameters());
            view.stopAnimation();
            new Scene(view, WIDTH, HEIGHT);
            view.resize(WIDTH, HEIGHT);
            view.layout();
            Samples.Sample sample = Samples.generate(size);
            view.show(sample.graph(), sample.cards(), sample.pointNotes());
            view.settle();
            double fit = view.camera().scale();
            for (double scale : new double[]{fit, 0.08, 0.2, 0.42, 0.72, 1.0}) {
                view.camera().zoomAtCursor(WIDTH / 2.0, HEIGHT / 2.0, scale / view.camera().scale());
                view.settle();
                report(view, size, scale, "stable");
                view.levelTransition = 0.5;
                report(view, size, scale, "transition");
                view.levelTransition = 1;
            }
        }
    }

    private void report(GraphView view, int size, double scale, String phase) {
        view.canvas().snapshot(null, null);
        long start = System.nanoTime();
        int frames = 10;
        for (int i = 0; i < frames; i++) {
            view.drawNow();
            view.canvas().snapshot(null, null);
        }
        System.out.printf("BENCH %5d cards  zoom %.3f  %-10s %6.1f ms  (%d drawn)%n",
                size, scale, phase, (System.nanoTime() - start) / 1e6 / frames, view.drawn());
    }

    private void captureLinkedElements(Path folder) throws IOException {
        GraphView view = new GraphView(new Parameters());
        view.stopAnimation();
        new Scene(view, WIDTH, HEIGHT);
        view.resize(WIDTH, HEIGHT);
        view.layout();
        Samples.Sample sample = Samples.generate(10);
        view.show(sample.graph(), sample.cards(), sample.pointNotes());
        view.settle();
        view.selected = "c0000";
        GraphView.Visual card = view.visuals.get("c0000");
        Camera c = view.camera();
        c.zoomAtCursor(c.screenX(card.x), c.screenY(card.y), 1 / c.scale());
        view.settle();
        if (!"c0000".equals(view.focus) || view.focusProgress != 1) {
            failure = new AssertionError("Linked elements did not reach the focused card: " + view.focus);
        }
        view.drawNow();
        ImageIO.write(toBuffered(view.canvas().snapshot(null, null)), "png", folder.resolve("linked-elements.png").toFile());
    }

    private void dragFromHandleCreatesConnection() {
        GraphView view = new GraphView(new Parameters());
        view.stopAnimation();
        new Scene(view, WIDTH, HEIGHT);
        view.resize(WIDTH, HEIGHT);
        view.layout();
        Samples.Sample empty = Samples.generate(0);
        view.show(empty.graph(), empty.cards(), empty.pointNotes());
        view.createLooseCard(-300, 0);
        view.createLooseCard(300, 0);
        view.settle();
        Camera c = view.camera();
        double fromX = c.screenX(-300 + Node.WIDTH / 2);
        double fromY = c.screenY(0);
        double toX = c.screenX(300);
        fire(view, MouseEvent.MOUSE_PRESSED, fromX, fromY);
        fire(view, MouseEvent.MOUSE_DRAGGED, toX, fromY);
        fire(view, MouseEvent.MOUSE_RELEASED, toX, fromY);
        if (view.graph.connections().size() != 1 || view.graph.node("loose-1").fixed() != null) {
            failure = new AssertionError("Dragging from the handle did not connect: " + view.graph.connections());
        }

        view.createLooseCard(0, 600);
        view.settle();
        double startX = c.screenX(0);
        double startY = c.screenY(600);
        double endX = c.screenX(view.visuals.get("loose-1").x);
        double endY = c.screenY(view.visuals.get("loose-1").y);
        fire(view, MouseEvent.MOUSE_PRESSED, startX, startY, MouseButton.SECONDARY);
        fire(view, MouseEvent.MOUSE_DRAGGED, endX, endY, MouseButton.SECONDARY);
        fire(view, MouseEvent.MOUSE_RELEASED, endX, endY, MouseButton.SECONDARY);
        if (!view.graph.dependenciesOf("loose-1").equals(java.util.List.of("loose-3"))) {
            failure = new AssertionError("Right drag did not connect: " + view.graph.connections());
        }
    }

    private void fire(GraphView view, EventType<MouseEvent> type, double x, double y) {
        fire(view, type, x, y, MouseButton.PRIMARY);
    }

    private void fire(GraphView view, EventType<MouseEvent> type, double x, double y, MouseButton button) {
        Event.fireEvent(view.canvas(), new MouseEvent(type, x, y, x, y, button, 1, false, false, false, false,
                button == MouseButton.PRIMARY, false, button == MouseButton.SECONDARY, false, false, false, null));
    }

    private void capture(Path folder, LayoutType type, int size, Double zoom, String name) throws IOException {
        GraphView view = new GraphView(new Parameters());
        view.stopAnimation();
        new Scene(view, WIDTH, HEIGHT);
        view.resize(WIDTH, HEIGHT);
        view.layout();
        Samples.Sample sample = Samples.generate(size);
        view.show(sample.graph(), sample.cards(), sample.pointNotes());
        view.reorganize(type);
        view.settle();
        view.frameImmediately();
        if (zoom != null) {
            double factor = zoom / view.camera().scale();
            view.camera().zoomAtCursor(WIDTH / 2.0, HEIGHT / 2.0, factor);
        }
        view.settle();
        view.drawNow();
        WritableImage image = view.canvas().snapshot(null, null);
        ImageIO.write(toBuffered(image), "png", folder.resolve(name + ".png").toFile());
    }

    private BufferedImage toBuffered(WritableImage image) {
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = image.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                output.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        return output;
    }
}

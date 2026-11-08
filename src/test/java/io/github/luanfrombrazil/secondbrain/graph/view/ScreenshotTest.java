package io.github.luanfrombrazil.secondbrain.graph.view;

import io.github.luanfrombrazil.secondbrain.graph.Parameters;
import javafx.application.Platform;
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
    }

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

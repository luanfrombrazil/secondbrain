package io.github.luanfrombrazil.secondbrain.app;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.track.Track;
import io.github.luanfrombrazil.secondbrain.vault.Vault;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Only runs with: mvn test -Dcapture=true -Dtest=MainViewScreenshotTest */
@EnabledIfSystemProperty(named = "capture", matches = "true")
class MainViewScreenshotTest {

    @TempDir
    Path root;

    @Test
    void mainScreenShowsAVaultAndAcceptsAConnection() throws Exception {
        Path folder = Path.of("target", "captures");
        Files.createDirectories(folder);
        try (Vault vault = Vault.open(root)) {
            Track track = vault.createTrack("Engenharia de Computação");
            LevelEditor setup = new LevelEditor(vault, track);
            Card first = setup.createCard("Cálculo I", null);
            Card second = setup.createCard("Cálculo II", null);
            Card third = setup.createCard("Estatística", null);
            setup.connect(first.id(), second.id());
            setup.createCard("Card solto", new double[]{500, 400});

            CompletableFuture<Void> done = new CompletableFuture<>();
            Platform.startup(() -> {
                try {
                    MainView view = new MainView(vault);
                    Scene scene = new Scene(view, 1500, 900);
                    scene.getStylesheets().add(MainView.class.getResource("secondbrain.css").toExternalForm());
                    view.connect(second.id(), third.id());
                    view.connect(first.id(), third.id());
                    view.openCard(first.id());
                    view.applyCss();
                    view.layout();
                    view.graphView().settle();
                    view.graphView().drawNow();
                    write(scene.snapshot(null), folder.resolve("main-view.png"));
                    done.complete(null);
                } catch (Throwable e) {
                    done.completeExceptionally(e);
                }
            });
            done.get(60, TimeUnit.SECONDS);
            assertEquals(2, third.dependencies().size());
        }
    }

    private static void write(WritableImage image, Path file) throws java.io.IOException {
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = image.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                output.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        ImageIO.write(output, "png", file.toFile());
    }
}

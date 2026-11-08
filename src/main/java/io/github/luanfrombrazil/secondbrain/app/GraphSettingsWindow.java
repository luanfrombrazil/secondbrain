package io.github.luanfrombrazil.secondbrain.app;

import io.github.luanfrombrazil.secondbrain.graph.layout.Layout;
import io.github.luanfrombrazil.secondbrain.graph.layout.Tunable;
import io.github.luanfrombrazil.secondbrain.graph.view.ViewParameters;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/** The "Graph" settings: the numbers the user tunes, saved with the vault. */
final class GraphSettingsWindow {

    private static final String[] ZOOM_LABELS = {"Bolinha → miniatura", "Miniatura → médio", "Médio → próximo"};

    private GraphSettingsWindow() {
    }

    static void apply(Map<String, Object> saved, List<Layout> layouts, ViewParameters view) {
        for (Layout layout : layouts) {
            for (Tunable tunable : layout.tunables()) {
                if (saved.get(key(layout, tunable)) instanceof Number value) {
                    tunable.setter().accept(value.doubleValue());
                }
            }
        }
        if (saved.get("arrowWidth") instanceof Number value) {
            view.arrowWidth = value.doubleValue();
        }
        for (int i = 0; i < ZOOM_LABELS.length; i++) {
            if (saved.get("zoom" + i) instanceof Number value) {
                view.zoomThresholds[i] = value.doubleValue();
            }
        }
    }

    static Map<String, Object> collect(List<Layout> layouts, ViewParameters view) {
        Map<String, Object> values = new HashMap<>();
        for (Layout layout : layouts) {
            for (Tunable tunable : layout.tunables()) {
                values.put(key(layout, tunable), tunable.getter().getAsDouble());
            }
        }
        values.put("arrowWidth", view.arrowWidth);
        for (int i = 0; i < ZOOM_LABELS.length; i++) {
            values.put("zoom" + i, view.zoomThresholds[i]);
        }
        return values;
    }

    /** @param onLayoutChange receives the layout whose setting changed; onViewChange takes the others */
    static void show(Window owner, List<Layout> layouts, ViewParameters view,
                     java.util.function.Consumer<Layout> onLayoutChange, Runnable onViewChange) {
        VBox box = new VBox(4);
        box.setStyle("-fx-padding: 14;");
        for (Layout layout : layouts) {
            if (!layout.tunables().isEmpty()) {
                title(box, layout.name());
                for (Tunable tunable : layout.tunables()) {
                    slider(box, tunable.label(), tunable.min(), tunable.max(), tunable.getter(), value -> {
                        tunable.setter().accept(value);
                        onLayoutChange.accept(layout);
                    });
                }
            }
        }
        title(box, "Setas");
        slider(box, "Espessura", 0.5, 8, () -> view.arrowWidth, value -> {
            view.arrowWidth = value;
            onViewChange.run();
        });
        title(box, "Zoom semântico");
        for (int i = 0; i < ZOOM_LABELS.length; i++) {
            int index = i;
            slider(box, ZOOM_LABELS[i], 0.05, 1.5, () -> view.zoomThresholds[index], value -> {
                view.zoomThresholds[index] = value;
                onViewChange.run();
            });
        }
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        Scene scene = new Scene(scroll, 360, 640);
        scene.getStylesheets().addAll(owner.getScene().getStylesheets());
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.setTitle("Configurações · Grafo");
        stage.setScene(scene);
        stage.show();
    }

    private static String key(Layout layout, Tunable tunable) {
        return layout.name() + " / " + tunable.label();
    }

    private static void title(VBox box, String text) {
        Label label = new Label(text);
        label.getStyleClass().add("group-title");
        box.getChildren().add(label);
    }

    private static void slider(VBox box, String text, double min, double max, DoubleSupplier getter, DoubleConsumer setter) {
        Label label = new Label();
        label.getStyleClass().add("label-text");
        Slider slider = new Slider(min, max, getter.getAsDouble());
        Runnable write = () -> label.setText(String.format(Locale.ROOT, "%s: %.2f", text, slider.getValue()));
        write.run();
        slider.valueProperty().addListener((o, a, value) -> {
            write.run();
            setter.accept(value.doubleValue());
        });
        box.getChildren().addAll(label, slider);
    }
}

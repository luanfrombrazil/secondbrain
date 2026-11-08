package io.github.luanfrombrazil.secondbrain.graph.lab;

import io.github.luanfrombrazil.secondbrain.graph.Parameters;
import io.github.luanfrombrazil.secondbrain.graph.view.Samples;
import io.github.luanfrombrazil.secondbrain.graph.view.Backdrop;
import io.github.luanfrombrazil.secondbrain.graph.view.GraphView;
import io.github.luanfrombrazil.secondbrain.graph.view.LayoutType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/** The graph with sample data and controls to tune the physics live. */
public final class GraphLab extends BorderPane {

    private final Parameters parameters = new Parameters();
    private final GraphView view = new GraphView(parameters);
    private final VBox controls = new VBox(4);
    private final Label readout = new Label();

    public GraphLab() {
        setCenter(view);
        buildControls();
        ScrollPane scroll = new ScrollPane(controls);
        scroll.setFitToWidth(true);
        scroll.setPrefWidth(310);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox panel = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        panel.getStyleClass().add("panel");
        setRight(panel);
        load(50);

        Timeline clock = new Timeline(new KeyFrame(Duration.millis(250), e -> updateReadout()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void load(int size) {
        Samples.Sample sample = Samples.generate(size);
        view.show(sample.graph(), sample.cards(), sample.pointNotes());
    }

    private void buildControls() {
        controls.setStyle("-fx-padding: 12;");

        title("Exemplo");
        ComboBox<LayoutType> layout = new ComboBox<>();
        layout.getItems().addAll(LayoutType.values());
        layout.setValue(LayoutType.DEPENDENCY);
        layout.setMaxWidth(Double.MAX_VALUE);
        layout.setOnAction(e -> view.reorganize(layout.getValue()));

        ComboBox<Backdrop> backdrop = new ComboBox<>();
        backdrop.getItems().addAll(Backdrop.values());
        backdrop.setValue(Backdrop.GRID);
        backdrop.setMaxWidth(Double.MAX_VALUE);
        backdrop.setOnAction(e -> view.setBackdrop(backdrop.getValue()));

        ComboBox<Integer> size = new ComboBox<>();
        size.getItems().addAll(0, 10, 50, 200, 500, 2000);
        size.setValue(50);
        size.setMaxWidth(Double.MAX_VALUE);
        size.setOnAction(e -> load(size.getValue()));

        label("Disposição");
        controls.getChildren().add(layout);
        label("Fundo");
        controls.getChildren().add(backdrop);
        label("Número de cards");
        controls.getChildren().add(size);

        Button reorganize = new Button("Reorganizar");
        reorganize.setOnAction(e -> view.reorganize(layout.getValue()));
        Button recenter = new Button("Centralizar");
        recenter.setOnAction(e -> view.recenter());
        Button copy = new Button("Copiar valores");
        copy.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(parameters.asText());
            Clipboard.getSystemClipboard().setContent(content);
        });
        HBox buttons = new HBox(6, reorganize, recenter, copy);
        buttons.setStyle("-fx-padding: 8 0 0 0;");
        controls.getChildren().add(buttons);

        checkBox("Conexões", view::setShowConnections);
        checkBox("Links (só de perto)", view::setShowLinks);
        checkBox("Agrupar por pasta", view::setGroupByFolder);

        title("Por dependência");
        layoutSlider("Folga entre colunas", 30, 400, () -> parameters.columnGap, v -> parameters.columnGap = v);
        layoutSlider("Folga entre linhas", 5, 200, () -> parameters.rowGap, v -> parameters.rowGap = v);

        title("Radial");
        layoutSlider("Distância entre anéis", 260, 900, () -> parameters.ringStep, v -> parameters.ringStep = v);

        title("Por forças");
        layoutSlider("Repulsão", 1000, 80000, () -> parameters.repulsion, v -> parameters.repulsion = v);
        layoutSlider("Força da mola", 0.01, 0.6, () -> parameters.springStrength, v -> parameters.springStrength = v);
        layoutSlider("Comprimento da mola", 100, 900, () -> parameters.springLength, v -> parameters.springLength = v);
        layoutSlider("Gravidade", 0, 0.12, () -> parameters.gravity, v -> parameters.gravity = v);
        layoutSlider("Atrito (velocidade mantida)", 0.2, 0.95, () -> parameters.friction, v -> parameters.friction = v);
        layoutSlider("Resfriamento", 0.002, 0.08, () -> parameters.cooling, v -> parameters.cooling = v);
        layoutSlider("Atração da pasta", 0, 0.2, () -> parameters.folderForce, v -> parameters.folderForce = v);

        title("Card solto (clique duplo no vazio)");
        layoutSlider("Raio magnético", 0, 240, () -> parameters.magneticRadius, v -> parameters.magneticRadius = v);

        title("Zoom semântico (escala de cada troca)");
        String[] names = {"Bolinha → miniatura", "Miniatura → afastado", "Afastado → médio", "Médio → próximo"};
        for (int i = 0; i < names.length; i++) {
            int index = i;
            zoomSlider(names[i], () -> parameters.zoomThresholds[index], v -> parameters.zoomThresholds[index] = v);
        }

        title("Medidas");
        readout.getStyleClass().add("readout");
        controls.getChildren().add(readout);
    }

    private void title(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("group-title");
        controls.getChildren().add(label);
    }

    private void label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label-text");
        controls.getChildren().add(label);
    }

    private void checkBox(String text, Consumer<Boolean> action) {
        CheckBox checkBox = new CheckBox(text);
        checkBox.setSelected(true);
        checkBox.selectedProperty().addListener((o, a, value) -> action.accept(value));
        controls.getChildren().add(checkBox);
    }

    private void layoutSlider(String text, double min, double max, DoubleSupplier getter, Consumer<Double> setter) {
        addSlider(text, min, max, getter, value -> {
            setter.accept(value);
            view.recomputeFollowingCenter();
        });
    }

    private void zoomSlider(String text, DoubleSupplier getter, Consumer<Double> setter) {
        addSlider(text, 0.05, 1.5, getter, value -> {
            setter.accept(value);
            view.markDirty();
        });
    }

    private void addSlider(String text, double min, double max, DoubleSupplier getter, Consumer<Double> onChange) {
        Label label = new Label();
        label.getStyleClass().add("label-text");
        Slider slider = new Slider(min, max, getter.getAsDouble());
        Runnable write = () -> label.setText(text + ": " + formatNumber(slider.getValue()));
        write.run();
        slider.valueProperty().addListener((o, a, value) -> {
            write.run();
            onChange.accept(value.doubleValue());
        });
        controls.getChildren().addAll(label, slider);
    }

    private static String formatNumber(double value) {
        return String.format(Locale.ROOT, value >= 100 ? "%.0f" : value >= 1 ? "%.1f" : "%.3f", value);
    }

    private void updateReadout() {
        readout.setText(String.format(Locale.ROOT, "Zoom: %.0f%%%nNível: %s%nCards: %d / %d%nFPS: %.0f%nSimulando: %s",
                view.camera().scale() * 100, view.level(), view.drawn(), view.totalCards(), view.fps(),
                view.simulating() ? "sim" : "não"));
    }
}

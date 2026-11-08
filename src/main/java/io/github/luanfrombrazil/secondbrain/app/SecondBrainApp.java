package io.github.luanfrombrazil.secondbrain.app;

import io.github.luanfrombrazil.secondbrain.graph.lab.GraphLab;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Opens the graph lab (implementation phase 1). */
public class SecondBrainApp extends Application {

    @Override
    public void start(Stage window) {
        Scene scene = new Scene(new GraphLab(), 1500, 900);
        scene.getStylesheets().add(getClass().getResource("secondbrain.css").toExternalForm());
        window.setTitle("SecondBrain · Laboratório do grafo");
        window.setScene(scene);
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

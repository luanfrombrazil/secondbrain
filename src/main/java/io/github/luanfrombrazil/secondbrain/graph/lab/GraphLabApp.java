package io.github.luanfrombrazil.secondbrain.graph.lab;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** The graph lab on its own, for future tests. Run with: mvn javafx:run -Plab */
public class GraphLabApp extends Application {

    @Override
    public void start(Stage window) {
        Scene scene = new Scene(new GraphLab(), 1500, 900);
        scene.getStylesheets().add(getClass()
                .getResource("/io/github/luanfrombrazil/secondbrain/app/secondbrain.css").toExternalForm());
        window.setTitle("SecondBrain · Laboratório do grafo");
        window.setScene(scene);
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

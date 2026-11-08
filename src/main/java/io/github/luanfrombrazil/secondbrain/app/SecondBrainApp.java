package io.github.luanfrombrazil.secondbrain.app;

import io.github.luanfrombrazil.secondbrain.grafo.lab.LaboratorioDoGrafo;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Abre o laboratório do grafo (fase 1 da implementação). */
public class SecondBrainApp extends Application {

    @Override
    public void start(Stage janela) {
        Scene cena = new Scene(new LaboratorioDoGrafo(), 1500, 900);
        cena.getStylesheets().add(getClass().getResource("secondbrain.css").toExternalForm());
        janela.setTitle("SecondBrain · Laboratório do grafo");
        janela.setScene(cena);
        janela.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

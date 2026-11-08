package io.github.luanfrombrazil.secondbrain.app;

import io.github.luanfrombrazil.secondbrain.trilha.TrilhaView;
import io.github.luanfrombrazil.secondbrain.trilha.disposicao.MenuDeDisposicao;
import io.github.luanfrombrazil.secondbrain.trilha.fundo.MenuDeFundo;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SecondBrainApp extends Application {

    @Override
    public void start(Stage palco) {
        TrilhaView trilha = new TrilhaView(Exemplo.engenharia());
        MenuDeDisposicao menu = new MenuDeDisposicao(trilha);
        StackPane.setAlignment(menu, Pos.TOP_LEFT);
        StackPane.setMargin(menu, new Insets(12));
        MenuDeFundo menuDeFundo = new MenuDeFundo(trilha);
        StackPane.setAlignment(menuDeFundo, Pos.TOP_RIGHT);
        StackPane.setMargin(menuDeFundo, new Insets(12));

        Scene cena = new Scene(new StackPane(trilha, menu, menuDeFundo), 1280, 800);
        cena.getStylesheets().add(getClass().getResource("secondbrain.css").toExternalForm());
        palco.setTitle("SecondBrain");
        palco.setScene(cena);
        palco.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

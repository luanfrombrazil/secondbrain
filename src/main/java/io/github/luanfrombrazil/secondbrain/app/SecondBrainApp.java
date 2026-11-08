package io.github.luanfrombrazil.secondbrain.app;

import io.github.luanfrombrazil.secondbrain.vault.Vault;
import io.github.luanfrombrazil.secondbrain.vault.VaultInUseException;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.Preferences;

/** Opens the last vault, or asks for one. */
public class SecondBrainApp extends Application {

    private static final String LAST_VAULT = "lastVault";

    private final Preferences preferences = Preferences.userNodeForPackage(SecondBrainApp.class);
    private final Label message = new Label();
    private Stage window;
    private Vault vault;

    @Override
    public void start(Stage stage) {
        window = stage;
        Scene scene = new Scene(startScreen(), 1500, 900);
        scene.getStylesheets().add(getClass().getResource("secondbrain.css").toExternalForm());
        window.setTitle("SecondBrain");
        window.setScene(scene);
        window.show();

        String last = preferences.get(LAST_VAULT, null);
        if (last != null && Files.isDirectory(Path.of(last))) {
            openVault(Path.of(last));
        }
    }

    private Parent startScreen() {
        Label title = new Label("SecondBrain");
        title.getStyleClass().add("group-title");
        Button create = new Button("Criar um acervo novo");
        create.setOnAction(e -> chooseFolder("Escolha a pasta do novo acervo"));
        Button open = new Button("Abrir uma pasta existente");
        open.setOnAction(e -> chooseFolder("Escolha a pasta do acervo"));
        message.getStyleClass().add("label-text");
        VBox box = new VBox(12, title, create, open, message);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void chooseFolder(String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        File folder = chooser.showDialog(window);
        if (folder != null) {
            openVault(folder.toPath());
        }
    }

    private void openVault(Path folder) {
        try {
            vault = Vault.open(folder);
            window.getScene().setRoot(new MainView(vault));
            window.setTitle("SecondBrain · " + folder.getFileName());
            preferences.put(LAST_VAULT, folder.toString());
        } catch (VaultInUseException e) {
            message.setText("Este acervo já está aberto em outra janela.");
        } catch (IOException e) {
            message.setText("Não foi possível abrir o acervo: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws IOException {
        if (vault != null) {
            vault.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

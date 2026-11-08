package io.github.luanfrombrazil.secondbrain.app;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.graph.layout.Layout;
import io.github.luanfrombrazil.secondbrain.graph.layout.Layouts;
import io.github.luanfrombrazil.secondbrain.graph.view.GraphEditor;
import io.github.luanfrombrazil.secondbrain.graph.view.GraphView;
import io.github.luanfrombrazil.secondbrain.graph.view.ViewParameters;
import io.github.luanfrombrazil.secondbrain.graph.view.backdrop.Backdrop;
import io.github.luanfrombrazil.secondbrain.graph.view.backdrop.Backdrops;
import io.github.luanfrombrazil.secondbrain.note.NoteEditorPane;
import io.github.luanfrombrazil.secondbrain.note.NotePaper;
import io.github.luanfrombrazil.secondbrain.track.Track;
import io.github.luanfrombrazil.secondbrain.vault.Vault;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** The main screen: explorer on the left, the graph of the open level in the middle. */
final class MainView extends BorderPane implements GraphEditor {

    private static final KeyCombination UNDO = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination REDO = new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN);

    /** A row of the explorer. */
    private record Entry(Track track, Card card, Card parent) {
        String label() {
            return card == null ? track.name() : card.title();
        }
    }

    private final Vault vault;
    private final List<Layout> layouts = Layouts.createAll();
    private final ViewParameters parameters = new ViewParameters();
    private final GraphView graphView = new GraphView(parameters, layouts.getFirst());
    private final Map<Track, LevelEditor> editors = new HashMap<>();
    private final TreeView<Entry> explorer = new TreeView<>();
    private final HBox path = new HBox(4);
    private final Pane overlay = new Pane();
    private final StackPane graphArea = new StackPane(graphView, overlay);
    private final BorderPane noteArea = new BorderPane();
    private final Label status = new Label();
    private final TextField newTrackName = new TextField();
    private LevelEditor editor;
    private CardEditorPane cardEditor;
    private NoteEditorPane noteEditor;

    MainView(Vault vault) throws IOException {
        this.vault = vault;
        GraphSettingsWindow.apply(vault.readSettings(), layouts, parameters);
        graphView.setEditor(this);
        overlay.setPickOnBounds(false);

        setTop(topBar());
        setLeft(explorerPanel());
        setCenter(graphArea);
        status.getStyleClass().add("label-text");
        status.setPadding(new Insets(4, 10, 4, 10));
        setBottom(status);

        addEventFilter(KeyEvent.KEY_PRESSED, this::onKey);
        rebuildExplorer();
        if (!vault.tracks().isEmpty()) {
            openTrack(vault.tracks().getFirst());
        } else {
            status.setText("Crie uma trilha para começar.");
        }
    }

    GraphView graphView() {
        return graphView;
    }

    private HBox topBar() {
        ComboBox<Layout> layout = new ComboBox<>();
        layout.getItems().addAll(layouts);
        layout.setValue(layouts.getFirst());
        layout.setConverter(named(Layout::name));
        layout.setOnAction(e -> graphView.reorganize(layout.getValue()));

        ComboBox<Backdrop> backdrop = new ComboBox<>();
        backdrop.getItems().addAll(Backdrops.ALL);
        backdrop.setValue(Backdrops.DEFAULT);
        backdrop.setConverter(named(Backdrop::name));
        backdrop.setOnAction(e -> graphView.setBackdrop(backdrop.getValue()));

        Button settings = new Button("Configurações");
        settings.setOnAction(e -> GraphSettingsWindow.show(getScene().getWindow(), layouts, parameters, this::onLayoutSetting, this::onViewSetting));

        Pane space = new Pane();
        HBox.setHgrow(space, Priority.ALWAYS);
        Button preamble = new Button("Preâmbulo");
        preamble.setOnAction(e -> editPreamble());
        HBox bar = new HBox(8, path, space, layout, backdrop, preamble, settings);
        bar.setPadding(new Insets(6, 10, 6, 10));
        bar.getStyleClass().add("panel");
        return bar;
    }

    private void editPreamble() {
        if (editor == null) {
            status.setText("Crie uma trilha antes de editar o preâmbulo.");
            return;
        }
        Track track = editor.track();
        TextArea text = new TextArea(track.preamble());
        Button save = new Button("Salvar");
        Label hint = new Label("Vale para todas as notas da trilha, ao abrir cada uma.");
        hint.getStyleClass().add("label-text");
        Stage stage = new Stage();
        save.setOnAction(e -> attempt(() -> {
            track.setPreamble(text.getText());
            vault.saveTrack(track);
            stage.close();
        }));
        VBox box = new VBox(8, text, hint, save);
        box.setPadding(new Insets(10));
        VBox.setVgrow(text, Priority.ALWAYS);
        Scene scene = new Scene(box, 620, 420);
        scene.getStylesheets().addAll(getScene().getStylesheets());
        stage.initOwner(getScene().getWindow());
        stage.setTitle("Preâmbulo · " + track.name());
        stage.setScene(scene);
        stage.show();
    }

    private VBox explorerPanel() {
        explorer.setShowRoot(false);
        explorer.setCellFactory(tree -> new TreeCell<>() {
            @Override
            protected void updateItem(Entry entry, boolean empty) {
                super.updateItem(entry, empty);
                setText(empty || entry == null ? null : entry.label());
            }
        });
        explorer.getSelectionModel().selectedItemProperty().addListener((o, a, item) -> {
            if (item != null && item.getValue() != null) {
                reveal(item.getValue());
            }
        });
        VBox.setVgrow(explorer, Priority.ALWAYS);

        newTrackName.setPromptText("Nova trilha: nome e Enter");
        newTrackName.setOnAction(e -> createTrack());
        VBox panel = new VBox(6, newTrackName, explorer);
        panel.setPadding(new Insets(8));
        panel.setPrefWidth(260);
        panel.getStyleClass().add("panel");
        return panel;
    }

    private void createTrack() {
        String name = newTrackName.getText().strip();
        if (name.isEmpty()) {
            return;
        }
        attempt(() -> {
            Track track = vault.createTrack(name);
            newTrackName.clear();
            rebuildExplorer();
            openTrack(track);
        });
    }

    private void openTrack(Track track) throws IOException {
        closeNoteEditor();
        closeCardEditor();
        editor = editors.containsKey(track) ? editors.get(track) : new LevelEditor(vault, track);
        editors.put(track, editor);
        editor.open(null);
        showLevel(true);
    }

    private void reveal(Entry entry) {
        attempt(() -> {
            if (editor == null || editor.track() != entry.track()) {
                openTrack(entry.track());
            }
            if (entry.card() == null) {
                editor.open(null);
                showLevel(false);
                return;
            }
            if (editor.parent() != entry.parent()) {
                editor.open(entry.parent());
                showLevel(true);
            }
            graphView.focusCard(entry.card().id());
        });
    }

    /** @param fresh true when the level changed, so the camera frames it */
    private void showLevel(boolean fresh) {
        TrackGraphBridge.View data = TrackGraphBridge.build(editor.track(), editor.level());
        if (fresh) {
            graphView.show(data.graph(), data.cards(), List.of());
        } else {
            graphView.refresh(data.graph(), data.cards(), List.of());
        }
        rebuildPath();
    }

    private void rebuildPath() {
        path.getChildren().clear();
        Button root = new Button(editor.track().name());
        root.setOnAction(e -> {
            editor.open(null);
            showLevel(true);
        });
        path.getChildren().add(root);
        if (editor.parent() != null) {
            Label inner = new Label("›  " + editor.parent().title());
            inner.getStyleClass().add("label-text");
            path.getChildren().add(inner);
        }
    }

    private void rebuildExplorer() {
        TreeItem<Entry> root = new TreeItem<>();
        for (Track track : vault.tracks()) {
            TreeItem<Entry> item = new TreeItem<>(new Entry(track, null, null));
            item.setExpanded(true);
            addCards(item, track, null, track.cards());
            root.getChildren().add(item);
        }
        explorer.setRoot(root);
    }

    private void addCards(TreeItem<Entry> into, Track track, Card parent, List<Card> cards) {
        for (Card card : new ArrayList<>(cards)) {
            TreeItem<Entry> item = new TreeItem<>(new Entry(track, card, parent));
            item.setExpanded(true);
            addCards(item, track, card, card.innerCards());
            into.getChildren().add(item);
        }
    }

    @Override
    public void createCardAt(double worldX, double worldY, double screenX, double screenY) {
        if (editor == null) {
            status.setText("Crie uma trilha antes de criar cards.");
            return;
        }
        TextField title = new TextField();
        title.setPromptText("Título do card");
        title.setPrefWidth(220);
        title.relocate(screenX, screenY);
        overlay.getChildren().setAll(title);
        title.setOnAction(e -> {
            String text = title.getText().strip();
            overlay.getChildren().clear();
            if (!text.isEmpty()) {
                attempt(() -> {
                    editor.createCard(text, new double[]{worldX, worldY});
                    afterEdit();
                });
            }
        });
        title.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                overlay.getChildren().clear();
            }
        });
        title.focusedProperty().addListener((o, was, focused) -> {
            if (!focused) {
                overlay.getChildren().clear();
            }
        });
        title.requestFocus();
    }

    @Override
    public void connect(String sourceId, String targetId) {
        attempt(() -> {
            if (editor.connect(sourceId, targetId)) {
                afterEdit();
            }
        });
    }

    /** One click: the note peeks at the right of the graph, unless an editor is open there. */
    @Override
    public void cardSelected(String id) {
        if (cardEditor != null || noteEditor != null) {
            return;
        }
        Card card = id == null ? null
                : editor.level().stream().filter(c -> c.id().equals(id)).findFirst().orElse(null);
        if (card == null || !Files.exists(vault.pdfPath(card))) {
            setRight(null);
            return;
        }
        setRight(new NotePaper(vault.pdfPath(card), () -> openNoteEditor(card)));
    }

    /** Double click: opens the card's editor. */
    @Override
    public void openCard(String id) {
        editor.level().stream().filter(c -> c.id().equals(id)).findFirst().ifPresent(card -> {
            cardEditor = new CardEditorPane(editor, card, new CardEditorPane.Host() {
                @Override
                public void edited() {
                    afterEdit();
                }

                @Override
                public void showError(IOException error) {
                    status.setText("Erro: " + error.getMessage());
                }

                @Override
                public void close() {
                    closeCardEditor();
                }

                @Override
                public void openNote(Card card) {
                    openNoteEditor(card);
                }

                @Override
                public void openInnerGraph(Card card) {
                    closeNoteEditor();
                    closeCardEditor();
                    editor.open(card);
                    showLevel(true);
                }
            });
            setRight(cardEditor);
        });
    }

    private void openNoteEditor(Card card) {
        attempt(() -> {
            closeNoteEditor();
            vault.fillIfEmpty(editor.track(), card);
            noteEditor = new NoteEditorPane(vault.notePath(card), vault.cacheFolder(card),
                    editor.track().preamble(), card.title(), this::closeNoteEditor);
            // As in the screens: while writing, the graph gives way to the editor and the preview.
            noteArea.setCenter(noteEditor);
            noteArea.setLeft(cardEditor);
            setRight(null);
            setCenter(noteArea);
        });
    }

    private void closeNoteEditor() {
        if (noteEditor != null) {
            noteEditor.saveNow();
            noteEditor = null;
            noteArea.setCenter(null);
            noteArea.setLeft(null);
            setCenter(graphArea);
            setRight(cardEditor);
        }
    }

    private void closeCardEditor() {
        cardEditor = null;
        noteArea.setLeft(null);
        setRight(null);
    }

    private void afterEdit() {
        status.setText("");
        showLevel(false);
        rebuildExplorer();
        if (cardEditor != null) {
            if (editor.level().contains(cardEditor.card())) {
                cardEditor.refresh();
            } else {
                closeCardEditor();
            }
        }
    }

    private void onKey(KeyEvent event) {
        if (event.getTarget() instanceof TextField || editor == null) {
            return;
        }
        if (UNDO.match(event)) {
            attempt(() -> {
                if (editor.undo()) {
                    afterEdit();
                }
            });
            event.consume();
        } else if (REDO.match(event)) {
            attempt(() -> {
                if (editor.redo()) {
                    afterEdit();
                }
            });
            event.consume();
        } else if (event.getCode() == KeyCode.DELETE && graphView.selectedCard() != null) {
            attempt(() -> {
                if (editor.deleteCard(graphView.selectedCard())) {
                    afterEdit();
                } else {
                    status.setText("Ainda não dá para excluir um card com grafo interno.");
                }
            });
            event.consume();
        }
    }

    private void onLayoutSetting(Layout layout) {
        if (graphView.currentLayout() == layout) {
            graphView.recomputeFollowingCenter();
        }
        saveSettings();
    }

    private void onViewSetting() {
        graphView.markDirty();
        saveSettings();
    }

    private void saveSettings() {
        attempt(() -> vault.saveSettings(GraphSettingsWindow.collect(layouts, parameters)));
    }

    private interface Action {
        void run() throws IOException;
    }

    /** Disk problems are shown in the status bar; the app keeps running. */
    private void attempt(Action action) {
        try {
            action.run();
        } catch (IOException e) {
            status.setText("Erro: " + e.getMessage());
        }
    }

    private static <T> StringConverter<T> named(Function<T, String> name) {
        return new StringConverter<>() {
            @Override
            public String toString(T item) {
                return item == null ? "" : name.apply(item);
            }

            @Override
            public T fromString(String text) {
                return null;
            }
        };
    }
}

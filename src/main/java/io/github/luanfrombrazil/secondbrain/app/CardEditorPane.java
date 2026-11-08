package io.github.luanfrombrazil.secondbrain.app;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.card.CardType;
import io.github.luanfrombrazil.secondbrain.property.ProgressProperty;
import io.github.luanfrombrazil.secondbrain.property.Property;
import io.github.luanfrombrazil.secondbrain.property.SelectionProperty;
import io.github.luanfrombrazil.secondbrain.property.StateProperty;
import io.github.luanfrombrazil.secondbrain.property.TasksProperty;
import io.github.luanfrombrazil.secondbrain.property.TextProperty;
import io.github.luanfrombrazil.secondbrain.track.Selection;
import io.github.luanfrombrazil.secondbrain.track.StateOption;
import io.github.luanfrombrazil.secondbrain.track.Track;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/** A simple panel to edit what a card holds. Its look is provisional. */
final class CardEditorPane extends ScrollPane {

    /** What the panel asks of the main screen. */
    interface Host {
        void edited();

        void showError(IOException error);

        void close();

        void openInnerGraph(Card card);

        void openNote(Card card);
    }

    private interface Change {
        void run() throws IOException;
    }

    /** An entry of a combo box that may also mean "none". */
    private record Choice(String id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static final String NONE = "—";

    private final LevelEditor editor;
    private final Card card;
    private final Host host;
    private final VBox box = new VBox(6);

    CardEditorPane(LevelEditor editor, Card card, Host host) {
        this.editor = editor;
        this.card = card;
        this.host = host;
        box.setPadding(new Insets(12));
        setContent(box);
        setFitToWidth(true);
        setPrefWidth(340);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        getStyleClass().add("panel");
        refresh();
    }

    Card card() {
        return card;
    }

    /** Draws everything again from the card. */
    void refresh() {
        box.getChildren().clear();
        Track track = editor.track();

        Button close = new Button("Fechar");
        close.setOnAction(e -> host.close());
        Button inner = new Button("Grafo interno");
        inner.setOnAction(e -> host.openInnerGraph(card));
        Button note = new Button("Nota");
        note.setOnAction(e -> host.openNote(card));
        box.getChildren().add(new HBox(6, close, inner, note));

        title("Título");
        TextField titleField = new TextField(card.title());
        commitOnEnterOrLeave(titleField, text -> {
            if (!text.isBlank() && !text.equals(card.title())) {
                apply(() -> editor.setTitle(card, text.strip()));
            }
        });
        box.getChildren().add(titleField);

        stateSection(track);
        typeSection(track);
        tasksSection();
        selectionsSection(track);
        textsSection();
    }

    private void stateSection(Track track) {
        title("Estado");
        List<Choice> choices = new java.util.ArrayList<>();
        choices.add(new Choice(null, NONE));
        for (StateOption option : track.states()) {
            choices.add(new Choice(option.id(), option.name()));
        }
        String current = card.property(StateProperty.class).map(StateProperty::optionId).orElse(null);
        box.getChildren().add(combo(choices, current, id -> apply(() -> editor.setState(card, id))));
    }

    private void typeSection(Track track) {
        title("Tipo");
        List<Choice> choices = new java.util.ArrayList<>();
        choices.add(new Choice(null, NONE));
        for (CardType type : track.types()) {
            choices.add(new Choice(type.id(), type.name()));
        }
        box.getChildren().add(combo(choices, card.typeId(), id -> apply(() -> editor.setType(card, id))));
        box.getChildren().add(addField("Novo tipo: nome e Enter", name -> apply(() -> {
            CardType type = editor.createType(name);
            editor.setType(card, type.id());
        })));
    }

    private void tasksSection() {
        title("Tarefas");
        CheckBox progress = new CheckBox("Mostrar barra de progresso");
        progress.setSelected(card.property(ProgressProperty.class).isPresent());
        progress.setOnAction(e -> apply(() -> editor.setProgressShown(card, progress.isSelected())));
        box.getChildren().add(progress);

        List<TasksProperty.Task> tasks = card.property(TasksProperty.class).map(TasksProperty::tasks).orElse(List.of());
        for (int i = 0; i < tasks.size(); i++) {
            int index = i;
            CheckBox task = new CheckBox(tasks.get(i).text());
            task.setSelected(tasks.get(i).done());
            task.setOnAction(e -> apply(() -> editor.setTaskDone(card, index, task.isSelected())));
            box.getChildren().add(rowWithRemove(task, () -> apply(() -> editor.removeTask(card, index))));
        }
        box.getChildren().add(addField("Nova tarefa e Enter", text -> apply(() -> editor.addTask(card, text))));
    }

    private void selectionsSection(Track track) {
        title("Seleções");
        for (Selection selection : track.selections()) {
            label(selection.name());
            List<Choice> choices = new java.util.ArrayList<>();
            choices.add(new Choice(null, NONE));
            selection.options().forEach(option -> choices.add(new Choice(option.id(), option.name())));
            String current = card.properties().stream()
                    .filter(p -> p instanceof SelectionProperty s && s.selectionId().equals(selection.id()))
                    .map(p -> ((SelectionProperty) p).optionId())
                    .findFirst().orElse(null);
            box.getChildren().add(combo(choices, current,
                    id -> apply(() -> editor.setSelection(card, selection.id(), id))));
        }
        box.getChildren().add(addField("Nova seleção: Nome: op1, op2 e Enter", text -> {
            int colon = text.indexOf(':');
            if (colon <= 0) {
                return;
            }
            List<String> options = Arrays.stream(text.substring(colon + 1).split(","))
                    .map(String::strip).filter(s -> !s.isEmpty()).toList();
            apply(() -> editor.createSelection(text.substring(0, colon).strip(), options));
        }));
    }

    private void textsSection() {
        title("Textos");
        for (Property property : card.properties()) {
            if (property instanceof TextProperty text) {
                Label label = new Label(text.name() + ": " + text.value());
                label.getStyleClass().add("label-text");
                label.setWrapText(true);
                box.getChildren().add(rowWithRemove(label, () -> apply(() -> editor.removeProperty(card, text))));
            }
        }
        box.getChildren().add(addField("Novo texto: Nome: valor e Enter", text -> {
            int colon = text.indexOf(':');
            if (colon > 0) {
                apply(() -> editor.addText(card, text.substring(0, colon).strip(), text.substring(colon + 1).strip()));
            }
        }));
    }

    private ComboBox<Choice> combo(List<Choice> choices, String currentId, Consumer<String> onChange) {
        ComboBox<Choice> combo = new ComboBox<>();
        combo.getItems().addAll(choices);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setValue(choices.stream().filter(c -> java.util.Objects.equals(c.id(), currentId)).findFirst()
                .orElse(choices.getFirst()));
        combo.setOnAction(e -> onChange.accept(combo.getValue().id()));
        return combo;
    }

    private HBox rowWithRemove(javafx.scene.Node content, Runnable remove) {
        Button button = new Button("×");
        button.setOnAction(e -> remove.run());
        HBox.setHgrow(content, Priority.ALWAYS);
        HBox row = new HBox(6, content, button);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private TextField addField(String prompt, Consumer<String> onSubmit) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setOnAction(e -> {
            String text = field.getText().strip();
            if (!text.isEmpty()) {
                onSubmit.accept(text);
            }
        });
        return field;
    }

    private void commitOnEnterOrLeave(TextField field, Consumer<String> onCommit) {
        field.setOnAction(e -> onCommit.accept(field.getText()));
        field.focusedProperty().addListener((o, was, focused) -> {
            if (!focused) {
                onCommit.accept(field.getText());
            }
        });
    }

    private void title(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("group-title");
        box.getChildren().add(label);
    }

    private void label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label-text");
        box.getChildren().add(label);
    }

    private void apply(Change change) {
        try {
            change.run();
            host.edited();
        } catch (IOException e) {
            host.showError(e);
        }
    }
}

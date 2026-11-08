package io.github.luanfrombrazil.secondbrain.propriedade.tarefas;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Tarefa {

    private final StringProperty texto = new SimpleStringProperty();
    private final BooleanProperty feita = new SimpleBooleanProperty();

    public Tarefa(String texto, boolean feita) {
        this.texto.set(texto);
        this.feita.set(feita);
    }

    public StringProperty textoProperty() {
        return texto;
    }

    public BooleanProperty feitaProperty() {
        return feita;
    }

    public boolean isFeita() {
        return feita.get();
    }
}

package io.github.luanfrombrazil.secondbrain.propriedade.tarefas;

import io.github.luanfrombrazil.secondbrain.propriedade.ContaProgresso;
import io.github.luanfrombrazil.secondbrain.propriedade.Propriedade;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

/** O checklist do card. */
public class Tarefas implements Propriedade, ContaProgresso {

    // O extractor faz a lista avisar quando uma tarefa é marcada, e não só quando entra ou sai.
    private final ObservableList<Tarefa> lista =
            FXCollections.observableArrayList(t -> new Observable[] {t.feitaProperty(), t.textoProperty()});

    public Tarefas adicionar(String texto, boolean feita) {
        lista.add(new Tarefa(texto, feita));
        return this;
    }

    public ObservableList<Tarefa> lista() {
        return lista;
    }

    @Override
    public String nome() {
        return "Tarefas";
    }

    @Override
    public int total() {
        return lista.size();
    }

    @Override
    public int feitos() {
        return (int) lista.stream().filter(Tarefa::isFeita).count();
    }

    @Override
    public Node visaoNoCard() {
        VBox caixa = new VBox();
        caixa.getStyleClass().add("tarefas");
        for (Tarefa tarefa : lista) {
            CheckBox check = new CheckBox();
            check.textProperty().bind(tarefa.textoProperty());
            check.selectedProperty().bindBidirectional(tarefa.feitaProperty());
            caixa.getChildren().add(check);
        }
        return caixa;
    }

    @Override
    public void aoMudar(Runnable acao) {
        lista.addListener((ListChangeListener<Tarefa>) mudanca -> acao.run());
    }
}

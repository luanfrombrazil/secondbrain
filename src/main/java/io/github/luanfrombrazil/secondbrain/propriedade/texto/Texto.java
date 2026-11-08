package io.github.luanfrombrazil.secondbrain.propriedade.texto;

import io.github.luanfrombrazil.secondbrain.propriedade.Propriedade;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;

/** Um campo livre com nome e valor, como "Período: 04/2". */
public class Texto implements Propriedade {

    private final String nome;
    private final StringProperty valor = new SimpleStringProperty();

    public Texto(String nome, String valor) {
        this.nome = nome;
        this.valor.set(valor);
    }

    public StringProperty valorProperty() {
        return valor;
    }

    @Override
    public String nome() {
        return nome;
    }

    @Override
    public Node visaoNoCard() {
        Label rotulo = new Label();
        rotulo.textProperty().bind(valor.map(v -> nome + ": " + v));
        rotulo.getStyleClass().add("texto");
        return rotulo;
    }

    @Override
    public void aoMudar(Runnable acao) {
        valor.addListener((obs, antes, depois) -> acao.run());
    }
}

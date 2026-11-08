package io.github.luanfrombrazil.secondbrain.card;

import io.github.luanfrombrazil.secondbrain.nota.Nota;
import io.github.luanfrombrazil.secondbrain.propriedade.ContaProgresso;
import io.github.luanfrombrazil.secondbrain.propriedade.Propriedade;
import io.github.luanfrombrazil.secondbrain.propriedade.Propriedades;
import io.github.luanfrombrazil.secondbrain.propriedade.progresso.Progresso;
import io.github.luanfrombrazil.secondbrain.trilha.Trilha;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Optional;
import java.util.OptionalDouble;

/** Uma nota no grafo. Um card sem tipo é uma nota pontual. */
public class Card {

    private final StringProperty titulo = new SimpleStringProperty();
    private final ObjectProperty<TipoDeCard> tipo = new SimpleObjectProperty<>();
    private final ObservableList<Propriedade> propriedades = FXCollections.observableArrayList();
    private final ObjectProperty<Nota> nota = new SimpleObjectProperty<>();
    private final BooleanProperty marcadoConcluido = new SimpleBooleanProperty();
    private final DoubleProperty x = new SimpleDoubleProperty();
    private final DoubleProperty y = new SimpleDoubleProperty();

    /** O grafo de dentro do card (Composite): uma trilha com os mesmos cards. Nulo se não tem. */
    private Trilha grafoInterno;

    public Card(String titulo) {
        this.titulo.set(titulo);
    }

    /** Cria um card com as propriedades iniciais do tipo. Depois disso, elas são do card. */
    public static Card doTipo(String titulo, TipoDeCard tipo) {
        Card card = new Card(titulo);
        card.tipo.set(tipo);
        tipo.propriedadesIniciais().forEach(id -> card.propriedades.add(Propriedades.criar(id)));
        return card;
    }

    public Card em(double x, double y) {
        this.x.set(x);
        this.y.set(y);
        return this;
    }

    public <P extends Propriedade> Optional<P> propriedade(Class<P> classe) {
        return propriedades.stream().filter(classe::isInstance).map(classe::cast).findFirst();
    }

    /**
     * Soma o que conta para o progresso (as tarefas, por exemplo) e os cards concluídos do grafo interno.
     * Vazio se o card não tem a propriedade Progresso.
     */
    public OptionalDouble progresso() {
        if (propriedade(Progresso.class).isEmpty()) {
            return OptionalDouble.empty();
        }
        int total = 0;
        int feitos = 0;
        for (Propriedade p : propriedades) {
            if (p instanceof ContaProgresso conta) {
                total += conta.total();
                feitos += conta.feitos();
            }
        }
        if (grafoInterno != null) {
            total += grafoInterno.cards().size();
            feitos += grafoInterno.concluidos();
        }
        return OptionalDouble.of(total == 0 ? 0 : (double) feitos / total);
    }

    public StringProperty tituloProperty() {
        return titulo;
    }

    public ObjectProperty<TipoDeCard> tipoProperty() {
        return tipo;
    }

    public ObservableList<Propriedade> propriedades() {
        return propriedades;
    }

    public ObjectProperty<Nota> notaProperty() {
        return nota;
    }

    public BooleanProperty marcadoConcluidoProperty() {
        return marcadoConcluido;
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public Trilha grafoInterno() {
        return grafoInterno;
    }

    public void setGrafoInterno(Trilha grafoInterno) {
        this.grafoInterno = grafoInterno;
    }
}

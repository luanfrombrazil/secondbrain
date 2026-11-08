package io.github.luanfrombrazil.secondbrain.trilha;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.card.Estado;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

/** Um grafo independente de cards. Também é o grafo interno de um card. */
public class Trilha {

    private final String nome;
    private final ObservableList<Card> cards = FXCollections.observableArrayList();
    private final ObservableList<Conexao> conexoes = FXCollections.observableArrayList();
    private final ObservableList<Link> links = FXCollections.observableArrayList();

    public Trilha(String nome) {
        this.nome = nome;
    }

    public Card adicionar(Card card) {
        cards.add(card);
        return card;
    }

    public void conectar(Card origem, Card destino) {
        conexoes.add(new Conexao(origem, destino));
    }

    public void ligar(Card a, Card b) {
        links.add(new Link(a, b));
    }

    public List<Card> dependenciasDe(Card card) {
        return conexoes.stream().filter(c -> c.destino() == card).map(Conexao::origem).toList();
    }

    public List<Card> dependentesDe(Card card) {
        return conexoes.stream().filter(c -> c.origem() == card).map(Conexao::destino).toList();
    }

    /** Todos os cards ligados a este, por conexão ou link, sem olhar a direção. */
    public List<Card> vizinhosDe(Card card) {
        List<Card> vizinhos = new ArrayList<>(dependenciasDe(card));
        vizinhos.addAll(dependentesDe(card));
        for (Link link : links) {
            if (link.a() == card) vizinhos.add(link.b());
            if (link.b() == card) vizinhos.add(link.a());
        }
        return vizinhos;
    }

    /**
     * Concluído se foi marcado ou chegou a 100%; bloqueado se alguma dependência não está concluída;
     * em andamento se já tem progresso; disponível no resto.
     */
    public Estado estadoDe(Card card) {
        var progresso = card.progresso();
        if (card.marcadoConcluidoProperty().get() || progresso.orElse(0) >= 1) {
            return Estado.CONCLUIDO;
        }
        boolean bloqueado = dependenciasDe(card).stream().anyMatch(d -> estadoDe(d) != Estado.CONCLUIDO);
        if (bloqueado) {
            return Estado.BLOQUEADO;
        }
        return progresso.orElse(0) > 0 ? Estado.EM_ANDAMENTO : Estado.DISPONIVEL;
    }

    public int concluidos() {
        return (int) cards.stream().filter(c -> estadoDe(c) == Estado.CONCLUIDO).count();
    }

    public String nome() {
        return nome;
    }

    public ObservableList<Card> cards() {
        return cards;
    }

    public ObservableList<Conexao> conexoes() {
        return conexoes;
    }

    public ObservableList<Link> links() {
        return links;
    }
}

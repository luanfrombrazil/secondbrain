package io.github.luanfrombrazil.secondbrain.app;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.card.TipoDeCard;
import io.github.luanfrombrazil.secondbrain.propriedade.tarefas.Tarefas;
import io.github.luanfrombrazil.secondbrain.propriedade.texto.Texto;
import io.github.luanfrombrazil.secondbrain.trilha.Trilha;

import java.util.List;

/** Dados de exemplo enquanto não existe salvar e carregar. */
final class Exemplo {

    private static final TipoDeCard DISCIPLINA =
            new TipoDeCard("Disciplina", "🎓", List.of("tarefas", "progresso"));

    private Exemplo() {
    }

    static Trilha engenharia() {
        Trilha trilha = new Trilha("Engenharia de Computação");

        Card calculo1 = trilha.adicionar(Card.doTipo("Cálculo I", DISCIPLINA).em(0, 0));
        tarefas(calculo1).adicionar("Lista 1", true).adicionar("Lista 2", true).adicionar("Prova", true);

        Card integracao = trilha.adicionar(Card.doTipo("Integração e Séries", DISCIPLINA).em(320, 0));
        tarefas(integracao).adicionar("Lista 1", true).adicionar("Lista 2", false).adicionar("Prova", false);
        integracao.propriedades().add(new Texto("Período", "03/1"));

        Card variaveis = trilha.adicionar(Card.doTipo("Várias Variáveis I", DISCIPLINA).em(640, 0));
        tarefas(variaveis).adicionar("Lista 1", false).adicionar("Prova", false);

        Card estatistica = trilha.adicionar(Card.doTipo("Estatística", DISCIPLINA).em(320, 240));
        tarefas(estatistica).adicionar("Lista 1", false);

        // Card sem tipo: uma nota pontual.
        Card livro = trilha.adicionar(new Card("O Andar do Bêbado").em(640, 300));

        trilha.conectar(calculo1, integracao);
        trilha.conectar(integracao, variaveis);
        trilha.conectar(calculo1, estatistica);
        trilha.ligar(estatistica, livro);

        // Composite: o grafo interno de Cálculo I também conta no progresso dele.
        Trilha interno = new Trilha("Cálculo I");
        interno.adicionar(new Card("Limites")).marcadoConcluidoProperty().set(true);
        interno.adicionar(new Card("Derivadas")).marcadoConcluidoProperty().set(true);
        calculo1.setGrafoInterno(interno);

        return trilha;
    }

    private static Tarefas tarefas(Card card) {
        return card.propriedade(Tarefas.class).orElseThrow();
    }
}

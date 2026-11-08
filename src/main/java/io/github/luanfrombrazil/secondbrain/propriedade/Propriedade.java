package io.github.luanfrombrazil.secondbrain.propriedade;

import javafx.scene.Node;

/**
 * Uma propriedade de um card (tarefas, bibliografia, vídeos, um campo de texto...).
 * <p>
 * Cada propriedade é um plugin: guarda os próprios dados e sabe se desenhar.
 * Uma propriedade nova é só mais uma implementação desta interface, registrada em {@link Propriedades}.
 */
public interface Propriedade {

    String nome();

    /** O que a propriedade mostra dentro do card, ou {@code null} se não mostra nada. */
    Node visaoNoCard();

    /** Chama {@code acao} sempre que os dados mudarem, para o card se redesenhar. */
    default void aoMudar(Runnable acao) {
    }
}

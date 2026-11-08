package io.github.luanfrombrazil.secondbrain.propriedade.progresso;

import io.github.luanfrombrazil.secondbrain.propriedade.Propriedade;
import javafx.scene.Node;

/**
 * Liga a barra de progresso no card. Não guarda dados: o valor é calculado
 * a partir das tarefas e do grafo interno (ver {@code Card.progresso()}).
 */
public class Progresso implements Propriedade {

    @Override
    public String nome() {
        return "Progresso";
    }

    @Override
    public Node visaoNoCard() {
        return null; // a barra fica no rodapé, desenhada pelo CardView
    }
}

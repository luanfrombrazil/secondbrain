package io.github.luanfrombrazil.secondbrain.trilha.disposicao;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.trilha.Trilha;
import javafx.geometry.Point2D;

import java.util.Map;

/** Um jeito de arrumar os cards de uma trilha. Cada algoritmo é uma implementação. */
public interface Disposicao {

    /** Largura e altura aproximadas de um card, para centralizar e espaçar. */
    double LARGURA_CARD = 220;
    double ALTURA_CARD = 160;

    String nome();

    /** Verdadeiro se a disposição continua agindo depois de aplicada (a física das forças). */
    default boolean viva() {
        return false;
    }

    /**
     * Calcula a nova posição (canto superior esquerdo) de cada card.
     *
     * @param foco o card em destaque, usado como centro pelo radial; pode ser nulo
     */
    Map<Card, Point2D> calcular(Trilha trilha, Card foco);
}

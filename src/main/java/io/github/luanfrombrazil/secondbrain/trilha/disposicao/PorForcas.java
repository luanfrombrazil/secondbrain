package io.github.luanfrombrazil.secondbrain.trilha.disposicao;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.trilha.Trilha;
import javafx.geometry.Point2D;

import java.util.Map;

/**
 * Layout por forças (no estilo Fruchterman-Reingold). Na tela ele fica "vivo": a {@code TrilhaView}
 * roda a {@link SimulacaoDeForcas} a cada quadro, e arrastar um card mexe nos vizinhos.
 * {@link #calcular} roda a mesma simulação até parar, para quem só quer as posições finais.
 */
public final class PorForcas implements Disposicao {

    @Override
    public String nome() {
        return "Forças";
    }

    @Override
    public boolean viva() {
        return true;
    }

    @Override
    public Map<Card, Point2D> calcular(Trilha trilha, Card foco) {
        SimulacaoDeForcas simulacao = new SimulacaoDeForcas(trilha);
        while (!simulacao.parada()) {
            simulacao.passo(null);
        }
        return simulacao.posicoes();
    }
}

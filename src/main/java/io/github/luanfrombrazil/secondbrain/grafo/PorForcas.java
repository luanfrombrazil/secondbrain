package io.github.luanfrombrazil.secondbrain.grafo;

import java.util.Map;

/** Roda a simulação até assentar, sem animar. A tela usa a simulação direto, quadro a quadro. */
public final class PorForcas implements Disposicao {

    @Override
    public Map<String, Ponto> calcular(Grafo grafo, Parametros parametros) {
        SimulacaoDeForcas simulacao = new SimulacaoDeForcas(grafo, parametros, Map.of());
        while (simulacao.passo()) {
            // até assentar
        }
        return simulacao.posicoes();
    }
}

package io.github.luanfrombrazil.secondbrain.grafo;

import java.util.Map;

/** Um jeito de arrumar o grafo: devolve o centro de cada nó. */
public interface Disposicao {

    Map<String, Ponto> calcular(Grafo grafo, Parametros parametros);
}

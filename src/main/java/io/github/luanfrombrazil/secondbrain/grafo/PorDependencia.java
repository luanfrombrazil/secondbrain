package io.github.luanfrombrazil.secondbrain.grafo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Colunas por profundidade: cada card fica à direita de tudo de que depende.
 * Um pai fica centrado entre os filhos; com um filho só, os dois ficam na mesma linha.
 */
public final class PorDependencia extends DisposicaoEmArvore {

    private static final int RODADAS_DE_ORDENACAO = 3;

    @Override
    protected Map<String, Ponto> arrumar(Grafo grafo, Floresta f, Parametros p) {
        double passoX = No.LARGURA + p.folgaHorizontal;
        double passoY = No.ALTURA + p.folgaVertical;

        Map<String, Double> ys = new HashMap<>();
        for (int rodada = 0; ; rodada++) {
            distribuir(f, passoY, ys);
            if (rodada == RODADAS_DE_ORDENACAO) {
                break;
            }
            ordenar(grafo, f, ys);
        }

        Map<String, Ponto> posicoes = new HashMap<>();
        ys.forEach((id, y) -> posicoes.put(id, new Ponto(grafo.camada(id) * passoX, y)));
        return posicoes;
    }

    private void distribuir(Floresta f, double passoY, Map<String, Double> ys) {
        ys.clear();
        double[] cursor = {0};
        for (String raiz : f.raizes) {
            colocar(raiz, f, passoY, ys, cursor);
            cursor[0] += passoY * 0.5;
        }
    }

    private double colocar(String id, Floresta f, double passoY, Map<String, Double> ys, double[] cursor) {
        List<String> filhos = f.filhos.get(id);
        double y;
        if (filhos.isEmpty()) {
            y = cursor[0];
            cursor[0] += passoY;
        } else {
            double primeiro = colocar(filhos.getFirst(), f, passoY, ys, cursor);
            double ultimo = primeiro;
            for (String filho : filhos.subList(1, filhos.size())) {
                ultimo = colocar(filho, f, passoY, ys, cursor);
            }
            y = (primeiro + ultimo) / 2;
        }
        ys.put(id, y);
        return y;
    }

    /** Aproxima cada card da altura média dos seus pré-requisitos, para cruzar menos setas. */
    private void ordenar(Grafo grafo, Floresta f, Map<String, Double> ys) {
        Comparator<String> ordem = Comparator
                .comparing((String id) -> grafo.no(id).pasta(), Floresta.SEM_PASTA_POR_ULTIMO)
                .thenComparingDouble(id -> grafo.dependenciasDe(id).stream()
                        .mapToDouble(ys::get).average().orElse(ys.get(id)))
                .thenComparing(Comparator.naturalOrder());
        f.raizes.sort(ordem);
        f.filhos.values().forEach(lista -> lista.sort(ordem));
    }
}

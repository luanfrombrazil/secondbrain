package io.github.luanfrombrazil.secondbrain.grafo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * O que as disposições em árvore têm em comum: quem tem conexão é arrumado pela subclasse,
 * o solto com ponto fixo fica lá e empurra os vizinhos, e os outros soltos vão para uma faixa.
 */
abstract class DisposicaoEmArvore implements Disposicao {

    private static final int POR_LINHA_DA_FAIXA = 8;

    protected abstract Map<String, Ponto> arrumar(Grafo grafo, Floresta floresta, Parametros parametros);

    @Override
    public final Map<String, Ponto> calcular(Grafo grafo, Parametros p) {
        Map<String, Ponto> posicoes = new HashMap<>(arrumar(grafo, new Floresta(grafo), p));
        porNaFaixa(grafo, posicoes, p);

        Set<String> fixos = new HashSet<>();
        for (No no : grafo.nos()) {
            if (grafo.solto(no.id()) && no.fixo() != null) {
                posicoes.put(no.id(), no.fixo());
                fixos.add(no.id());
            }
        }
        return AfastamentoMagnetico.aplicar(posicoes, fixos, p.raioMagnetico);
    }

    private void porNaFaixa(Grafo grafo, Map<String, Ponto> posicoes, Parametros p) {
        double esquerda = posicoes.values().stream().mapToDouble(Ponto::x).min().orElse(0);
        double baixo = posicoes.values().stream().mapToDouble(Ponto::y).max().orElse(0);
        double passoX = No.LARGURA + 30;
        double passoY = No.ALTURA + p.folgaVertical;
        double topoDaFaixa = posicoes.isEmpty() ? 0 : baixo + 1.6 * passoY;
        int i = 0;
        for (No no : grafo.nos()) {
            if (grafo.solto(no.id()) && no.fixo() == null) {
                posicoes.put(no.id(), new Ponto(
                        esquerda + (i % POR_LINHA_DA_FAIXA) * passoX,
                        topoDaFaixa + (i / POR_LINHA_DA_FAIXA) * passoY));
                i++;
            }
        }
    }
}

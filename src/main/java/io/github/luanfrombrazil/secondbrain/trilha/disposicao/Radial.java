package io.github.luanfrombrazil.secondbrain.trilha.disposicao;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.trilha.Trilha;
import javafx.geometry.Point2D;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Árvore circular ao redor de um card. Cada anel é uma distância (em saltos) até o centro,
 * e cada card recebe uma fatia do ângulo proporcional ao tamanho do seu ramo.
 * Cards sem caminho até o centro vão para um anel externo.
 */
public final class Radial implements Disposicao {

    private static final double ANEL = 340;

    @Override
    public String nome() {
        return "Radial";
    }

    @Override
    public Map<Card, Point2D> calcular(Trilha trilha, Card foco) {
        Map<Card, Point2D> posicoes = new HashMap<>();
        if (trilha.cards().isEmpty()) {
            return posicoes;
        }
        Card centro = foco != null && trilha.cards().contains(foco) ? foco : raiz(trilha);

        // Busca em largura: monta a árvore de quem foi alcançado primeiro a partir do centro.
        Map<Card, List<Card>> filhos = new LinkedHashMap<>();
        Map<Card, Integer> nivel = new HashMap<>();
        ArrayDeque<Card> fila = new ArrayDeque<>();
        filhos.put(centro, new ArrayList<>());
        nivel.put(centro, 0);
        fila.add(centro);
        while (!fila.isEmpty()) {
            Card atual = fila.poll();
            for (Card vizinho : trilha.vizinhosDe(atual)) {
                if (!nivel.containsKey(vizinho)) {
                    nivel.put(vizinho, nivel.get(atual) + 1);
                    filhos.put(vizinho, new ArrayList<>());
                    filhos.get(atual).add(vizinho);
                    fila.add(vizinho);
                }
            }
        }

        posicionar(centro, 0, 2 * Math.PI, filhos, nivel, posicoes);

        // Os que ficaram de fora vão para um anel além do último.
        List<Card> soltos = trilha.cards().stream().filter(c -> !nivel.containsKey(c)).toList();
        int ultimo = nivel.values().stream().max(Integer::compare).orElse(0);
        for (int i = 0; i < soltos.size(); i++) {
            double angulo = 2 * Math.PI * i / soltos.size();
            posicoes.put(soltos.get(i), ponto(ultimo + 1, angulo));
        }
        return posicoes;
    }

    private void posicionar(Card card, double inicio, double fim, Map<Card, List<Card>> filhos,
                            Map<Card, Integer> nivel, Map<Card, Point2D> posicoes) {
        posicoes.put(card, ponto(nivel.get(card), (inicio + fim) / 2));
        double total = filhos.get(card).stream().mapToInt(f -> folhas(f, filhos)).sum();
        double angulo = inicio;
        for (Card filho : filhos.get(card)) {
            double fatia = (fim - inicio) * folhas(filho, filhos) / total;
            posicionar(filho, angulo, angulo + fatia, filhos, nivel, posicoes);
            angulo += fatia;
        }
    }

    /** Quantas folhas há no ramo: decide o tamanho da fatia. */
    private int folhas(Card card, Map<Card, List<Card>> filhos) {
        List<Card> lista = filhos.get(card);
        return lista.isEmpty() ? 1 : lista.stream().mapToInt(f -> folhas(f, filhos)).sum();
    }

    /** Converte anel e ângulo no canto superior esquerdo do card, com o card centrado no ponto. */
    private Point2D ponto(int anel, double angulo) {
        double raio = anel * ANEL;
        return new Point2D(raio * Math.cos(angulo) - LARGURA_CARD / 2, raio * Math.sin(angulo) - ALTURA_CARD / 2);
    }

    /** Sem foco, o centro é o primeiro card que não depende de ninguém. */
    private Card raiz(Trilha trilha) {
        return trilha.cards().stream()
                .filter(c -> trilha.dependenciasDe(c).isEmpty())
                .findFirst()
                .orElse(trilha.cards().getFirst());
    }
}

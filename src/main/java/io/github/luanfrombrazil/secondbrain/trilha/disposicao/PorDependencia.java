package io.github.luanfrombrazil.secondbrain.trilha.disposicao;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.trilha.Trilha;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Layout em camadas (Sugiyama, versão simples).
 * <ol>
 *   <li>Coluna: o tamanho da maior cadeia de dependências até o card. Daí os "vazios que são esperas".</li>
 *   <li>Ordem dentro da coluna: cada card vai para a altura média dos vizinhos (baricentro),
 *       em algumas passadas de ida e volta, para diminuir os cruzamentos.</li>
 * </ol>
 */
public final class PorDependencia implements Disposicao {

    private static final double COLUNA = 320;
    private static final double LINHA = 220;
    private static final int PASSADAS = 4;

    @Override
    public String nome() {
        return "Dependência";
    }

    @Override
    public Map<Card, Point2D> calcular(Trilha trilha, Card foco) {
        Map<Card, Integer> coluna = new HashMap<>();
        for (Card card : trilha.cards()) {
            coluna(card, trilha, coluna, new HashSet<>());
        }

        TreeMap<Integer, List<Card>> colunas = new TreeMap<>();
        for (Card card : trilha.cards()) {
            colunas.computeIfAbsent(coluna.get(card), c -> new ArrayList<>()).add(card);
        }

        for (int i = 0; i < PASSADAS; i++) {
            ordenar(colunas, trilha, true);                 // da esquerda para a direita
            ordenar(colunas.descendingMap(), trilha, false); // e de volta
        }

        Map<Card, Point2D> posicoes = new HashMap<>();
        colunas.forEach((c, cards) -> {
            for (int i = 0; i < cards.size(); i++) {
                posicoes.put(cards.get(i), new Point2D(c * COLUNA, altura(i, cards.size()) * LINHA));
            }
        });
        return posicoes;
    }

    /** A maior cadeia de dependências até o card. Um ciclo é ignorado em vez de travar. */
    private int coluna(Card card, Trilha trilha, Map<Card, Integer> memoria, Set<Card> visitando) {
        Integer pronta = memoria.get(card);
        if (pronta != null) {
            return pronta;
        }
        if (!visitando.add(card)) {
            return 0;
        }
        int coluna = 0;
        for (Card dependencia : trilha.dependenciasDe(card)) {
            coluna = Math.max(coluna, coluna(dependencia, trilha, memoria, visitando) + 1);
        }
        visitando.remove(card);
        memoria.put(card, coluna);
        return coluna;
    }

    /** Baricentro: ordena cada coluna pela altura média dos vizinhos da coluna anterior na passada. */
    private void ordenar(Map<Integer, List<Card>> colunas, Trilha trilha, boolean paraDireita) {
        Map<Card, Double> altura = new HashMap<>();
        for (List<Card> cards : colunas.values()) {
            for (int i = 0; i < cards.size(); i++) {
                altura.put(cards.get(i), altura(i, cards.size()));
            }
        }
        for (List<Card> cards : colunas.values()) {
            Map<Card, Double> chave = new HashMap<>();
            for (Card card : cards) {
                List<Card> vizinhos = paraDireita ? trilha.dependenciasDe(card) : trilha.dependentesDe(card);
                chave.put(card, vizinhos.stream().mapToDouble(altura::get).average().orElse(altura.get(card)));
            }
            cards.sort(Comparator.comparingDouble(chave::get));
            for (int i = 0; i < cards.size(); i++) {
                altura.put(cards.get(i), altura(i, cards.size()));
            }
        }
    }

    /** Posição i numa coluna de n cards, centrada no zero. */
    private static double altura(int i, int n) {
        return i - (n - 1) / 2.0;
    }
}

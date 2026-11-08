package io.github.luanfrombrazil.secondbrain.grafo.tela;

import javafx.scene.paint.Color;

import java.util.List;

/**
 * O que o grafo mostra de um card. Na fase 2 vem do modelo real.
 *
 * @param peso de 0 a 1; decide o tamanho da bolinha
 */
public record Cartao(String id, String titulo, Color corDoEstado, double progresso, double peso,
                     List<String> tarefas, int tarefasFeitas, boolean temGrafoInterno) {
}

package io.github.luanfrombrazil.secondbrain.trilha;

import io.github.luanfrombrazil.secondbrain.card.Card;

/** Ordem, precedência e dependência: {@code destino} só fica disponível depois de {@code origem}. */
public record Conexao(Card origem, Card destino) {
}

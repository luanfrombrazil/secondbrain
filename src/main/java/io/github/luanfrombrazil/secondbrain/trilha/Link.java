package io.github.luanfrombrazil.secondbrain.trilha;

import io.github.luanfrombrazil.secondbrain.card.Card;

/** Um link entre dois cards, só para navegar. Não cria dependência. */
public record Link(Card a, Card b) {
}

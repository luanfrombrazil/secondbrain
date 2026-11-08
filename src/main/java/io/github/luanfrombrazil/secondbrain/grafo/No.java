package io.github.luanfrombrazil.secondbrain.grafo;

/**
 * O que as disposições precisam saber de um card.
 *
 * @param pasta pasta comum que agrupa o card, ou nulo
 * @param fixo  ponto onde o card solto foi criado, ou nulo
 */
public record No(String id, String pasta, Ponto fixo) {

    public static final double LARGURA = 220;
    public static final double ALTURA = 110;

    public No semFixo() {
        return new No(id, pasta, null);
    }
}

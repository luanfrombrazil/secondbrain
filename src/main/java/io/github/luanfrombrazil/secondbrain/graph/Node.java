package io.github.luanfrombrazil.secondbrain.graph;

/**
 * What the layouts need to know about a card.
 *
 * @param folder plain folder of the card, or null; only attracts its cards in the force layout
 * @param fixed  point where the loose card was created, or null
 */
public record Node(String id, String folder, Point fixed) {

    public static final double WIDTH = 220;
    public static final double HEIGHT = 110;

    public Node withoutFixed() {
        return new Node(id, folder, null);
    }
}

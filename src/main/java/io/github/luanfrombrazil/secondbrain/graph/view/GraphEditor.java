package io.github.luanfrombrazil.secondbrain.graph.view;

/** Receives what the user does on the canvas, so the graph does not need to know where data lives. */
public interface GraphEditor {

    /** A double click on empty space. */
    void createCardAt(double worldX, double worldY, double screenX, double screenY);

    /** A drag from one card to another: the source becomes a prerequisite of the target. */
    void connect(String sourceId, String targetId);

    /** A double click on a card. */
    void openCard(String id);
}

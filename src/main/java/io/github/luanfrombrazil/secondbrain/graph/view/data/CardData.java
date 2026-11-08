package io.github.luanfrombrazil.secondbrain.graph.view.data;

import javafx.scene.paint.Color;

import java.util.List;

/**
 * What the graph shows of a card. In phase 2 this comes from the real model.
 *
 * @param weight from 0 to 1; sets the size of the dot
 */
public record CardData(String id, String title, Color stateColor, double progress, double weight,
                     List<String> tasks, int tasksDone, boolean hasInnerGraph, boolean showsProgress) {
}

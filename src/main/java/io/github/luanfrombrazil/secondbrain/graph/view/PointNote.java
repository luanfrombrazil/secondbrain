package io.github.luanfrombrazil.secondbrain.graph.view;

import java.util.List;

/** Low-weight note: left out of the layout, shown up close next to the focused card. */
public record PointNote(String title, List<String> linkedTo) {
}

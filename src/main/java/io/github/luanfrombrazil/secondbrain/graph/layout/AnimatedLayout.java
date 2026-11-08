package io.github.luanfrombrazil.secondbrain.graph.layout;

import io.github.luanfrombrazil.secondbrain.graph.Graph;
import io.github.luanfrombrazil.secondbrain.graph.Point;

import java.util.Map;

/** A layout the view advances frame by frame, instead of moving the cards straight to the result. */
public interface AnimatedLayout extends Layout {

    /** @param from where the cards are now; missing cards get a starting point from the layout */
    LayoutRun start(Graph graph, Map<String, Point> from);
}

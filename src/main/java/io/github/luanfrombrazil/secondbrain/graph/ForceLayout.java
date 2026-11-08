package io.github.luanfrombrazil.secondbrain.graph;

import java.util.Map;

/** Runs the simulation until it settles, without animating. The view steps the simulation frame by frame. */
public final class ForceLayout implements Layout {

    @Override
    public Map<String, Point> compute(Graph graph, Parameters parameters) {
        ForceSimulation simulation = new ForceSimulation(graph, parameters, Map.of());
        while (simulation.step()) {
            // until it settles
        }
        return simulation.positions();
    }
}

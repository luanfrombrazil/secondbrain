package io.github.luanfrombrazil.secondbrain.graph.layout.force;

import io.github.luanfrombrazil.secondbrain.graph.Graph;
import io.github.luanfrombrazil.secondbrain.graph.Point;
import io.github.luanfrombrazil.secondbrain.graph.layout.AnimatedLayout;
import io.github.luanfrombrazil.secondbrain.graph.layout.LayoutRun;
import io.github.luanfrombrazil.secondbrain.graph.layout.Tunable;

import java.util.List;
import java.util.Map;

/** Cards repel each other and connections pull them together, settling over time. */
public final class ForceLayout implements AnimatedLayout {

    private final ForceSettings settings = new ForceSettings();

    @Override
    public String name() {
        return "Por forças";
    }

    @Override
    public List<Tunable> tunables() {
        return List.of(
                new Tunable("Repulsão", 1000, 80000, () -> settings.repulsion, v -> settings.repulsion = v),
                new Tunable("Força da mola", 0.01, 0.6, () -> settings.springStrength, v -> settings.springStrength = v),
                new Tunable("Comprimento da mola", 100, 900, () -> settings.springLength, v -> settings.springLength = v),
                new Tunable("Gravidade", 0, 0.12, () -> settings.gravity, v -> settings.gravity = v),
                new Tunable("Atrito (velocidade mantida)", 0.2, 0.95, () -> settings.friction, v -> settings.friction = v),
                new Tunable("Atração da pasta", 0, 0.2, () -> settings.folderForce, v -> settings.folderForce = v));
    }

    @Override
    public LayoutRun start(Graph graph, Map<String, Point> from) {
        return new ForceSimulation(graph, settings, from);
    }

    /** Runs until it settles, without animating. */
    @Override
    public Map<String, Point> compute(Graph graph) {
        LayoutRun run = start(graph, Map.of());
        while (run.step()) {
            // until it settles
        }
        return run.positions();
    }
}

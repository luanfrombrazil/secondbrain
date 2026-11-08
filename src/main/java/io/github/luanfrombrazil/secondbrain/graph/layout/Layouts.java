package io.github.luanfrombrazil.secondbrain.graph.layout;

import io.github.luanfrombrazil.secondbrain.graph.layout.dependency.DependencyLayout;
import io.github.luanfrombrazil.secondbrain.graph.layout.force.ForceLayout;
import io.github.luanfrombrazil.secondbrain.graph.layout.radial.RadialLayout;

import java.util.List;

/** The available layouts. This is the only place that knows all of them. */
public final class Layouts {

    private Layouts() {
    }

    /** New instances, each with its own default settings. The first one is the default layout. */
    public static List<Layout> createAll() {
        return List.of(new DependencyLayout(), new ForceLayout(), new RadialLayout());
    }
}

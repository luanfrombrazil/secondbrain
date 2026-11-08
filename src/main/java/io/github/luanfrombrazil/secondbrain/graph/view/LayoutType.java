package io.github.luanfrombrazil.secondbrain.graph.view;

import io.github.luanfrombrazil.secondbrain.graph.Layout;
import io.github.luanfrombrazil.secondbrain.graph.DependencyLayout;
import io.github.luanfrombrazil.secondbrain.graph.ForceLayout;
import io.github.luanfrombrazil.secondbrain.graph.RadialLayout;

public enum LayoutType {
    DEPENDENCY("Por dependência", new DependencyLayout()),
    FORCES("Por forças", new ForceLayout()),
    RADIAL("Radial", new RadialLayout());

    private final String name;
    private final Layout layout;

    LayoutType(String name, Layout layout) {
        this.name = name;
        this.layout = layout;
    }

    public Layout layout() {
        return layout;
    }

    @Override
    public String toString() {
        return name;
    }
}

package io.github.luanfrombrazil.secondbrain.graph.view.backdrop;

import io.github.luanfrombrazil.secondbrain.graph.view.backdrop.dotted.DottedBackdrop;
import io.github.luanfrombrazil.secondbrain.graph.view.backdrop.grid.GridBackdrop;
import io.github.luanfrombrazil.secondbrain.graph.view.backdrop.lined.LinedBackdrop;
import io.github.luanfrombrazil.secondbrain.graph.view.backdrop.plain.PlainBackdrop;

import java.util.List;

/** The available backdrops. This is the only place that knows all of them. */
public final class Backdrops {

    public static final Backdrop DEFAULT = new GridBackdrop();
    public static final List<Backdrop> ALL =
            List.of(new PlainBackdrop(), new LinedBackdrop(), new DottedBackdrop(), DEFAULT);

    private Backdrops() {
    }
}

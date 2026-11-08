package io.github.luanfrombrazil.secondbrain.graph.layout;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/** One setting of a layout, described so that a control can be built for it. */
public record Tunable(String label, double min, double max, DoubleSupplier getter, DoubleConsumer setter) {
}

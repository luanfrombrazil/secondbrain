package io.github.luanfrombrazil.secondbrain.graph.view;

import io.github.luanfrombrazil.secondbrain.graph.layout.Layout;
import io.github.luanfrombrazil.secondbrain.graph.layout.Layouts;
import io.github.luanfrombrazil.secondbrain.graph.lab.Samples;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Prints how long the sample and each layout take at the extreme size. */
@EnabledIfSystemProperty(named = "capture", matches = "true")
class HugeGraphTest {

    @Test
    void timeLayouts() {
        long start = System.nanoTime();
        Samples.Sample sample = Samples.generate(10000);
        System.out.printf("BENCH sample %.0f ms%n", (System.nanoTime() - start) / 1e6);
        for (Layout type : Layouts.createAll()) {
            start = System.nanoTime();
            type.compute(sample.graph());
            System.out.printf("BENCH %s %.0f ms%n", type.name(), (System.nanoTime() - start) / 1e6);
        }
    }
}

package io.github.luanfrombrazil.secondbrain.graph.view;

import io.github.luanfrombrazil.secondbrain.graph.LayoutType;
import io.github.luanfrombrazil.secondbrain.graph.lab.Samples;
import io.github.luanfrombrazil.secondbrain.graph.Parameters;
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
        for (LayoutType type : LayoutType.values()) {
            start = System.nanoTime();
            type.layout().compute(sample.graph(), new Parameters());
            System.out.printf("BENCH %s %.0f ms%n", type, (System.nanoTime() - start) / 1e6);
        }
    }
}

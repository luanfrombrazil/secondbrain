package io.github.luanfrombrazil.secondbrain.property;

import java.util.Map;

/** Marks that the card shows a progress bar. The value itself is derived from the tasks and inner cards. */
public final class ProgressProperty implements Property {

    public static final String KIND = "progress";

    @Override
    public String kind() {
        return KIND;
    }

    @Override
    public Map<String, Object> toData() {
        return Map.of();
    }
}

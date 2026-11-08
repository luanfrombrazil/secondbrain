package io.github.luanfrombrazil.secondbrain.property;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/** Knows every kind of property, to rebuild them from the card files. */
public final class Properties {

    private static final Map<String, Function<Map<String, Object>, Property>> READERS = Map.of(
            StateProperty.KIND, StateProperty::fromData,
            TasksProperty.KIND, TasksProperty::fromData,
            ProgressProperty.KIND, data -> new ProgressProperty(),
            TextProperty.KIND, TextProperty::fromData,
            SelectionProperty.KIND, SelectionProperty::fromData);

    private Properties() {
    }

    /** Empty for a kind this version does not know. */
    public static Optional<Property> fromData(String kind, Map<String, Object> data) {
        return Optional.ofNullable(READERS.get(kind)).map(reader -> reader.apply(data));
    }
}

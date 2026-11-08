package io.github.luanfrombrazil.secondbrain.property;

import java.util.Map;

/** A named piece of text, such as the official name of a course. */
public record TextProperty(String name, String value) implements Property {

    public static final String KIND = "text";

    static TextProperty fromData(Map<String, Object> data) {
        return new TextProperty((String) data.get("name"), (String) data.get("value"));
    }

    @Override
    public String kind() {
        return KIND;
    }

    @Override
    public Map<String, Object> toData() {
        return Map.of("name", name, "value", value);
    }
}

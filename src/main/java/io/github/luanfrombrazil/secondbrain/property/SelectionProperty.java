package io.github.luanfrombrazil.secondbrain.property;

import java.util.Map;

/** The value a card has for one of the track's selections, such as priority or semester. */
public record SelectionProperty(String selectionId, String optionId) implements Property {

    public static final String KIND = "selection";

    static SelectionProperty fromData(Map<String, Object> data) {
        return new SelectionProperty((String) data.get("selection"), (String) data.get("option"));
    }

    @Override
    public String kind() {
        return KIND;
    }

    @Override
    public Map<String, Object> toData() {
        return Map.of("selection", selectionId, "option", optionId);
    }
}

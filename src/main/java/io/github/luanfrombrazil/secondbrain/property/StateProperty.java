package io.github.luanfrombrazil.secondbrain.property;

import java.util.HashMap;
import java.util.Map;

/**
 * The state of a card: one of the track's state options.
 * A card blocked automatically remembers the option it had before, to get it back.
 */
public final class StateProperty implements Property {

    public static final String KIND = "state";

    private String optionId;
    private String previousOptionId;

    public StateProperty(String optionId) {
        this.optionId = optionId;
    }

    static StateProperty fromData(Map<String, Object> data) {
        StateProperty state = new StateProperty((String) data.get("option"));
        state.previousOptionId = (String) data.get("previous");
        return state;
    }

    public String optionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public String previousOptionId() {
        return previousOptionId;
    }

    public void setPreviousOptionId(String previousOptionId) {
        this.previousOptionId = previousOptionId;
    }

    @Override
    public String kind() {
        return KIND;
    }

    @Override
    public Map<String, Object> toData() {
        Map<String, Object> data = new HashMap<>();
        data.put("option", optionId);
        if (previousOptionId != null) {
            data.put("previous", previousOptionId);
        }
        return data;
    }
}

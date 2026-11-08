package io.github.luanfrombrazil.secondbrain.track;

/** One of the states a card of a track can have. The automatic rules only look at the category. */
public record StateOption(String id, String name, String color, Category category) {

    public enum Category {
        BLOCKED, AVAILABLE, IN_PROGRESS, DONE
    }
}

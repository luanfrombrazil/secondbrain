package io.github.luanfrombrazil.secondbrain.track;

import java.util.List;

/** A list of options the user created for a track, such as priority or semester. */
public record Selection(String id, String name, List<Option> options) {

    public record Option(String id, String name) {
    }
}

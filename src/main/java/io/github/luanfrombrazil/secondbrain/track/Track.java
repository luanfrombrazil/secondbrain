package io.github.luanfrombrazil.secondbrain.track;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.card.CardType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** A root graph of cards, with its own states, types and color. Inner graphs are not tracks (D-54). */
public final class Track {

    public static final String DEFAULT_PREAMBLE = "\\documentclass{article}\n\\usepackage{amsmath}\n\\usepackage{hyperref}\n";

    private final String id;
    private String name;
    private String color;
    private final String folder;
    private String preamble = DEFAULT_PREAMBLE;
    private final List<StateOption> states = new ArrayList<>();
    private final List<CardType> types = new ArrayList<>();
    private final List<Selection> selections = new ArrayList<>();
    private final List<Card> cards = new ArrayList<>();

    public Track(String id, String name, String color, String folder) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.folder = folder;
    }

    /** Automatic changes use the first option of the category, in the user's order (D-44). */
    public Optional<StateOption> firstOption(StateOption.Category category) {
        return states.stream().filter(s -> s.category() == category).findFirst();
    }

    public Optional<StateOption> option(String optionId) {
        return states.stream().filter(s -> s.id().equals(optionId)).findFirst();
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String color() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    /** The track's folder, relative to the vault. */
    public String folder() {
        return folder;
    }

    /** The start of every new note of this track (D-55). */
    public String preamble() {
        return preamble;
    }

    public void setPreamble(String preamble) {
        this.preamble = preamble;
    }

    public List<StateOption> states() {
        return states;
    }

    public List<CardType> types() {
        return types;
    }

    public List<Selection> selections() {
        return selections;
    }

    public List<Card> cards() {
        return cards;
    }
}

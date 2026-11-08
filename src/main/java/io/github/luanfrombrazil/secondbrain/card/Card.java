package io.github.luanfrombrazil.secondbrain.card;

import io.github.luanfrombrazil.secondbrain.property.Property;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** A note in a track. Holds only data; how it looks belongs to each visualization. */
public final class Card {

    private final String id;
    private String title;
    private String notePath;
    private String typeId;
    private final List<Property> properties = new ArrayList<>();
    private final Set<String> dependencies = new LinkedHashSet<>();
    private final Set<String> links = new LinkedHashSet<>();
    private final List<Card> innerCards = new ArrayList<>();
    private double[] fixedPoint;

    public Card(String id, String title, String notePath) {
        this.id = id;
        this.title = title;
        this.notePath = notePath;
    }

    public <P extends Property> Optional<P> property(Class<P> type) {
        return properties.stream().filter(type::isInstance).map(type::cast).findFirst();
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String notePath() {
        return notePath;
    }

    public void setNotePath(String notePath) {
        this.notePath = notePath;
    }

    public String typeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public List<Property> properties() {
        return properties;
    }

    /** The ids of the cards this one depends on. The connection is stored on the dependent (D-53). */
    public Set<String> dependencies() {
        return dependencies;
    }

    /** The ids of the elements linked by hand from this card. */
    public Set<String> links() {
        return links;
    }

    /** The cards of the inner graph. */
    public List<Card> innerCards() {
        return innerCards;
    }

    /** Where a loose card was created, as {x, y}, or null. */
    public double[] fixedPoint() {
        return fixedPoint;
    }

    public void setFixedPoint(double[] fixedPoint) {
        this.fixedPoint = fixedPoint;
    }
}

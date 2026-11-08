package io.github.luanfrombrazil.secondbrain.property;

import java.util.Map;

/** Something a card holds. To add a kind, implement this and register it in {@link Properties}. */
public interface Property {

    /** The name that identifies this kind of property in the card file. */
    String kind();

    /** The values to store in the card file, without the kind. */
    Map<String, Object> toData();
}

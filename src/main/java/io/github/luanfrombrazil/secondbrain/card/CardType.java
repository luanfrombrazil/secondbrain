package io.github.luanfrombrazil.secondbrain.card;

import java.util.List;

/**
 * A preset for new cards: the name and icon are shared, the properties are copied on creation.
 *
 * @param initialProperties the kinds of property a new card of this type starts with
 */
public record CardType(String id, String name, String icon, List<String> initialProperties) {
}

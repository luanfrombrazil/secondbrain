package io.github.luanfrombrazil.secondbrain.vault;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

/** Turns a title into a safe file name: "Cálculo II" becomes "calculo-ii" (D-47). */
public final class Slug {

    private static final String EMPTY_NAME = "nota";
    private static final Set<String> RESERVED_ON_WINDOWS = Set.of(
            "con", "prn", "aux", "nul",
            "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
            "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9");

    private Slug() {
    }

    public static String of(String title) {
        String slug = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (slug.isEmpty()) {
            return EMPTY_NAME;
        }
        return RESERVED_ON_WINDOWS.contains(slug) ? slug + "-nota" : slug;
    }

    /** The slug, followed by -2, -3... until it is free. */
    public static String unique(String title, Predicate<String> taken) {
        String base = of(title);
        String candidate = base;
        for (int i = 2; taken.test(candidate); i++) {
            candidate = base + "-" + i;
        }
        return candidate;
    }
}

package io.github.luanfrombrazil.secondbrain.note;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Colors a LaTeX source: commands, environments, math and comments. */
final class TexHighlighter {

    private static final Pattern PATTERN = Pattern.compile(
            "(?<COMMENT>(?<!\\\\)%[^\\n]*)"
                    + "|(?<MATH>\\$\\$.*?\\$\\$|(?<!\\\\)\\$[^$\\n]*\\$)"
                    + "|(?<ENV>\\\\(?:begin|end)\\{[^}]*\\})"
                    + "|(?<COMMAND>\\\\[a-zA-Z@]+\\*?)",
            Pattern.DOTALL);
    private static final String[] GROUPS = {"COMMENT", "MATH", "ENV", "COMMAND"};
    private static final String[] STYLES = {"tex-comment", "tex-math", "tex-environment", "tex-command"};

    private TexHighlighter() {
    }

    static StyleSpans<Collection<String>> styles(String text) {
        Matcher matcher = PATTERN.matcher(text);
        StyleSpansBuilder<Collection<String>> spans = new StyleSpansBuilder<>();
        int last = 0;
        while (matcher.find()) {
            String style = "";
            for (int i = 0; i < GROUPS.length; i++) {
                if (matcher.group(GROUPS[i]) != null) {
                    style = STYLES[i];
                }
            }
            spans.add(List.of(), matcher.start() - last);
            spans.add(List.of(style), matcher.end() - matcher.start());
            last = matcher.end();
        }
        spans.add(List.of(), text.length() - last);
        return spans.create();
    }
}

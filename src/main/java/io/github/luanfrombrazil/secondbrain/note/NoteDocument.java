package io.github.luanfrombrazil.secondbrain.note;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The file of a note against what the user edits. What comes before the user's text is the
 * track's preamble and the card's title, added on saving, so the file on disk is always a
 * complete document. A title, author or date written in the note takes priority over them.
 */
public final class NoteDocument {

    public static final String EMPTY_SOURCE = "\\begin{document}\n\\maketitle\n\n\n\\end{document}";

    private static final String BEGIN = "\\begin{document}";
    private static final String END = "\\end{document}";
    private static final String SOURCE_MARK = "% note\n";
    private static final Pattern OVERRIDE = Pattern.compile("(?m)^(?:[^%\\n]*?)(\\\\(?:title|author|date)\\{)");

    private NoteDocument() {
    }

    public static String compose(String preamble, String title, String source) {
        return header(preamble, title, source) + source + "\n";
    }

    /** What the user edits. A text with no \begin{document} is placed inside an empty document. */
    public static String source(String file) {
        int mark = file.indexOf(SOURCE_MARK);
        if (mark >= 0) {
            return file.substring(mark + SOURCE_MARK.length()).strip();
        }
        int begin = file.indexOf(BEGIN);
        if (begin < 0) {
            return file.isBlank() ? EMPTY_SOURCE : BEGIN + "\n" + file.strip() + "\n" + END;
        }
        return file.substring(begin).strip();
    }

    /** How many lines of the file come before the first line the user sees. */
    public static int headerLines(String preamble, String title, String source) {
        return (int) header(preamble, title, source).chars().filter(c -> c == '\n').count();
    }

    private static String header(String preamble, String title, String source) {
        String start = preamble.endsWith("\n") ? preamble : preamble + "\n";
        return start + "\n\\title{" + title + "}\n\\author{}\n" + overrides(source) + "\n" + SOURCE_MARK;
    }

    /** Copies the note's own commands to the header, so they count wherever they were written. */
    static String overrides(String source) {
        StringBuilder found = new StringBuilder();
        Matcher matcher = OVERRIDE.matcher(source);
        while (matcher.find()) {
            int end = closingBrace(source, matcher.end());
            if (end > 0) {
                found.append(source, matcher.start(1), end + 1).append('\n');
            }
        }
        return found.toString();
    }

    private static int closingBrace(String text, int from) {
        int depth = 1;
        for (int i = from; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '{') {
                depth++;
            } else if (c == '}' && --depth == 0) {
                return i;
            }
        }
        return -1;
    }
}

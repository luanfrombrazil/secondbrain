package io.github.luanfrombrazil.secondbrain.note;

/**
 * The file of a note against what the user edits. The user only sees the body; the file on
 * disk is always a complete document, built from the track's preamble, so it compiles anywhere.
 */
public final class NoteDocument {

    private static final String BEGIN = "\\begin{document}";
    private static final String END = "\\end{document}";
    private static final String MAKE_TITLE = "\\maketitle";

    private NoteDocument() {
    }

    public static String compose(String preamble, String title, String body) {
        return header(preamble, title) + body + "\n" + END + "\n";
    }

    /** What the user edits. A text with no document markers is all body. */
    public static String body(String file) {
        int begin = file.indexOf(BEGIN);
        if (begin < 0) {
            return file.strip();
        }
        int end = file.lastIndexOf(END);
        String inside = file.substring(begin + BEGIN.length(), end > begin ? end : file.length()).strip();
        return inside.startsWith(MAKE_TITLE) ? inside.substring(MAKE_TITLE.length()).strip() : inside;
    }

    /** How many lines of the file come before the first line of the body. */
    public static int headerLines(String preamble, String title) {
        return (int) header(preamble, title).chars().filter(c -> c == '\n').count();
    }

    private static String header(String preamble, String title) {
        String start = preamble.endsWith("\n") ? preamble : preamble + "\n";
        return start + "\n\\title{" + title + "}\n\\author{}\n\n" + BEGIN + "\n" + MAKE_TITLE + "\n\n";
    }
}

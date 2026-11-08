package io.github.luanfrombrazil.secondbrain.note;

/**
 * The file of a note against what the user edits. The note holds everything from
 * \begin{document} to \end{document}; what comes before it is the track's preamble and the
 * card's title, added on saving, so the file on disk is always a complete document.
 */
public final class NoteDocument {

    public static final String EMPTY_SOURCE = "\\begin{document}\n\\maketitle\n\n\n\\end{document}";

    private static final String BEGIN = "\\begin{document}";
    private static final String END = "\\end{document}";

    private NoteDocument() {
    }

    public static String compose(String preamble, String title, String source) {
        return header(preamble, title) + source + "\n";
    }

    /** What the user edits. A text with no \begin{document} is placed inside an empty document. */
    public static String source(String file) {
        int begin = file.indexOf(BEGIN);
        if (begin < 0) {
            return file.isBlank() ? EMPTY_SOURCE : BEGIN + "\n" + file.strip() + "\n" + END;
        }
        return file.substring(begin).strip();
    }

    /** How many lines of the file come before the first line the user sees. */
    public static int headerLines(String preamble, String title) {
        return (int) header(preamble, title).chars().filter(c -> c == '\n').count();
    }

    private static String header(String preamble, String title) {
        String start = preamble.endsWith("\n") ? preamble : preamble + "\n";
        return start + "\n\\title{" + title + "}\n\\author{}\n\n";
    }
}

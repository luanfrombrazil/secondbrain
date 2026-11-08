package io.github.luanfrombrazil.secondbrain.note;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoteDocumentTest {

    private static final String PREAMBLE = "\\documentclass{article}\n\\usepackage{amsmath}\n";

    @Test
    void theBodySurvivesComposingAndReading() {
        String file = NoteDocument.compose(PREAMBLE, "Cálculo", "Primeira linha.\n\nSegunda.");
        assertTrue(file.startsWith(PREAMBLE));
        assertTrue(file.contains("\\title{Cálculo}"));
        assertEquals("Primeira linha.\n\nSegunda.", NoteDocument.body(file));
    }

    @Test
    void headerLinesPointToTheFirstLineOfTheBody() {
        String file = NoteDocument.compose(PREAMBLE, "T", "corpo");
        int lines = NoteDocument.headerLines(PREAMBLE, "T");
        assertEquals("corpo", file.split("\n")[lines]);
    }

    @Test
    void anOldNoteWithItsOwnHeaderKeepsOnlyTheBody() {
        assertEquals("texto", NoteDocument.body("\\documentclass{book}\n\\begin{document}\ntexto\n\\end{document}\n"));
    }

    @Test
    void aTextWithoutMarkersIsAllBody() {
        assertEquals("só texto", NoteDocument.body("só texto\n"));
    }
}

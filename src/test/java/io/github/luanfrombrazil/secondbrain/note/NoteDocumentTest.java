package io.github.luanfrombrazil.secondbrain.note;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoteDocumentTest {

    private static final String PREAMBLE = "\\documentclass{article}\n\\usepackage{amsmath}\n";
    private static final String SOURCE = "\\begin{document}\nPrimeira linha.\n\\end{document}";

    @Test
    void theDocumentPartSurvivesComposingAndReading() {
        String file = NoteDocument.compose(PREAMBLE, "Cálculo", SOURCE);
        assertTrue(file.startsWith(PREAMBLE));
        assertTrue(file.contains("\\title{Cálculo}"));
        assertEquals(SOURCE, NoteDocument.source(file));
    }

    @Test
    void headerLinesPointToTheBeginningOfTheDocument() {
        String file = NoteDocument.compose(PREAMBLE, "T", SOURCE);
        assertEquals("\\begin{document}", file.split("\n")[NoteDocument.headerLines(PREAMBLE, "T", SOURCE)]);
    }

    @Test
    void titleAuthorAndDateInTheNoteOverrideTheGlobalOnes() {
        String source = "\\title{Meu {título}}\n\\begin{document}\n\\maketitle\n\\author{Eu}\n\\date{hoje}\n\\end{document}";
        String file = NoteDocument.compose(PREAMBLE, "Card", source);
        assertTrue(file.indexOf("\\title{Meu {título}}") > file.indexOf("\\title{Card}"));
        assertTrue(file.indexOf("\\author{Eu}") < file.indexOf("\\begin{document}"));
        assertTrue(file.indexOf("\\date{hoje}") < file.indexOf("\\begin{document}"));
        assertEquals(source, NoteDocument.source(file));
    }

    @Test
    void aCommentedTitleDoesNotOverride() {
        assertEquals("", NoteDocument.overrides("% \\title{Não}\n"));
    }

    @Test
    void anOldNoteLosesOnlyItsHeader() {
        assertEquals(SOURCE, NoteDocument.source("\\documentclass{book}\n" + SOURCE + "\n"));
    }

    @Test
    void anEmptyNoteStartsAsAnEmptyDocument() {
        assertEquals(NoteDocument.EMPTY_SOURCE, NoteDocument.source("  \n"));
    }

    @Test
    void aTextWithoutDocumentMarkersIsWrapped() {
        assertEquals("\\begin{document}\nsó texto\n\\end{document}", NoteDocument.source("só texto\n"));
    }
}

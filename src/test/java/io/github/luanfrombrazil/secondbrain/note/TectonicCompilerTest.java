package io.github.luanfrombrazil.secondbrain.note;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TectonicCompilerTest {

    @Test
    void errorsWithALineComeFirst() {
        List<TectonicCompiler.Problem> problems = TectonicCompiler.parseProblems("""
                note: Running TeX ...
                ! Undefined control sequence.
                l.5 \\comandoinexistente
                                       {x}
                No pages of output.
                error: the XeTeX engine had an unrecoverable error
                """);
        assertEquals(2, problems.size());
        assertEquals(5, problems.get(0).line());
        assertEquals("Undefined control sequence.", problems.get(0).message());
        assertEquals(0, problems.get(1).line());
    }

    @Test
    void aCleanLogHasNoProblems() {
        assertEquals(0, TectonicCompiler.parseProblems("note: Running TeX ...\nnote: Writing nota.pdf").size());
    }
}

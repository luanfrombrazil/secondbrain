package io.github.luanfrombrazil.secondbrain.note;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs the real program; skipped when tools/tectonic is not there. */
@EnabledIf("hasTectonic")
class TectonicIntegrationTest {

    @TempDir
    Path folder;

    static boolean hasTectonic() {
        return TectonicCompiler.find().isPresent();
    }

    @Test
    void aGoodNoteProducesAPdf() throws Exception {
        Path tex = folder.resolve("boa.tex");
        Files.writeString(tex, "\\documentclass{article}\n\\begin{document}\nOi.\n\\end{document}\n");
        TectonicCompiler.Result result = TectonicCompiler.find().orElseThrow().compile(tex, folder.resolve("saida"));
        assertTrue(result.succeeded(), result.log());
        assertTrue(Files.size(result.pdf()) > 0);
    }

    @Test
    void anErrorIsReportedWithItsLine() throws Exception {
        Path tex = folder.resolve("ruim.tex");
        Files.writeString(tex, "\\documentclass{article}\n\\begin{document}\nOi.\n\\naoexiste\n\\end{document}\n");
        TectonicCompiler.Result result = TectonicCompiler.find().orElseThrow().compile(tex, folder.resolve("saida"));
        assertFalse(result.succeeded());
        assertEquals(4, result.problems().getFirst().line());
    }
}

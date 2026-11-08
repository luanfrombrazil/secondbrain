package io.github.luanfrombrazil.secondbrain.nota;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** O texto de um card: um arquivo .md ou .tex comum. */
public interface Nota {

    Path arquivo();

    /** As divisões da nota (títulos, {@code \section}, {@code \chapter}): de onde vêm as notas fantasmas. */
    List<String> divisoes() throws IOException;
}

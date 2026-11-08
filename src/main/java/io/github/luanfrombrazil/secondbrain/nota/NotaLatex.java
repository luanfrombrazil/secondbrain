package io.github.luanfrombrazil.secondbrain.nota;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public record NotaLatex(Path arquivo) implements Nota {

    private static final Pattern DIVISAO = Pattern.compile("\\\\(?:chapter|section)\\*?\\{([^}]*)}");

    @Override
    public List<String> divisoes() throws IOException {
        return DIVISAO.matcher(Files.readString(arquivo)).results()
                .map(m -> m.group(1))
                .toList();
    }
}

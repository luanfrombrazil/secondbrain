package io.github.luanfrombrazil.secondbrain.nota;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record NotaMarkdown(Path arquivo) implements Nota {

    @Override
    public List<String> divisoes() throws IOException {
        return Files.readAllLines(arquivo).stream()
                .filter(linha -> linha.startsWith("#"))
                .map(linha -> linha.replaceFirst("^#+\\s*", ""))
                .toList();
    }
}

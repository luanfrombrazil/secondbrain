package io.github.luanfrombrazil.secondbrain.vault;

import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/** Reads and writes the small YAML files of the vault. A crash while writing never corrupts a file. */
final class YamlFiles {

    private YamlFiles() {
    }

    static void write(Path file, Map<String, Object> data) throws IOException {
        Files.createDirectories(file.getParent());
        DumpSettings settings = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).build();
        String yaml = new Dump(settings).dumpToString(data);
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temporary, yaml, StandardCharsets.UTF_8);
        Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> read(Path file) throws IOException {
        Load load = new Load(LoadSettings.builder().build());
        return (Map<String, Object>) load.loadFromString(Files.readString(file, StandardCharsets.UTF_8));
    }
}

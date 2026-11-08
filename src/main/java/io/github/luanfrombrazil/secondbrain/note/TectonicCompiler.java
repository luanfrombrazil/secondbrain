package io.github.luanfrombrazil.secondbrain.note;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Runs the Tectonic program. Never call it from the JavaFX thread. */
public final class TectonicCompiler {

    /** @param line 1-based, or 0 when the log does not say */
    public record Problem(int line, String message) {
    }

    /** @param pdf the produced file; null when the compilation failed */
    public record Result(Path pdf, List<Problem> problems, String log) {
        public boolean succeeded() {
            return pdf != null;
        }
    }

    private static final Pattern ERROR_LINE = Pattern.compile("^error: (?:.*?):(\\d+): (.*)$");
    private static final Pattern GENERAL_ERROR = Pattern.compile("^error: (.*)$");
    private static final long TIMEOUT_SECONDS = 180;
    private static final String PROPERTY = "secondbrain.tectonic";

    private final Path executable;

    public TectonicCompiler(Path executable) {
        this.executable = executable;
    }

    /** Looks in the system property, then in tools/tectonic next to the program. */
    public static Optional<TectonicCompiler> find() {
        String configured = System.getProperty(PROPERTY);
        Path candidate = configured != null ? Path.of(configured) : Path.of("tools", "tectonic", "tectonic.exe");
        return Files.isRegularFile(candidate) ? Optional.of(new TectonicCompiler(candidate.toAbsolutePath())) : Optional.empty();
    }

    public Result compile(Path texFile, Path outputFolder) throws IOException, InterruptedException {
        Files.createDirectories(outputFolder);
        Process process = new ProcessBuilder(executable.toString(), "-X", "compile", texFile.getFileName().toString(),
                "--synctex", "--outdir", outputFolder.toAbsolutePath().toString())
                .directory(texFile.toAbsolutePath().getParent().toFile())
                .redirectErrorStream(true)
                .start();
        StringBuilder log = new StringBuilder();
        Thread reader = Thread.ofVirtual().start(() -> {
            try {
                log.append(new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                log.append(e.getMessage());
            }
        });
        if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            return new Result(null, List.of(new Problem(0, "A compilação demorou demais.")), log.toString());
        }
        reader.join();
        String output = log.toString();
        String name = texFile.getFileName().toString().replaceFirst("\\.tex$", "") + ".pdf";
        Path pdf = outputFolder.resolve(name);
        return process.exitValue() == 0 && Files.exists(pdf)
                ? new Result(pdf, List.of(), output)
                : new Result(null, parseProblems(output), output);
    }

    /** The errors in Tectonic's output; the ones naming a line come first. */
    static List<Problem> parseProblems(String log) {
        List<Problem> withLine = new ArrayList<>();
        List<Problem> general = new ArrayList<>();
        for (String line : log.split("\\R")) {
            Matcher located = ERROR_LINE.matcher(line);
            Matcher other = GENERAL_ERROR.matcher(line);
            if (located.matches()) {
                withLine.add(new Problem(Integer.parseInt(located.group(1)), located.group(2)));
            } else if (other.matches()) {
                general.add(new Problem(0, other.group(1)));
            }
        }
        withLine.addAll(general);
        return withLine;
    }
}

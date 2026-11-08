package io.github.luanfrombrazil.secondbrain.note;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A note: the .tex on the left and its PDF on the right. Saves about a second after the user
 * stops typing; compiles only on Ctrl S or the button. A failed compilation never removes the last good PDF.
 */
public final class NoteEditorPane extends BorderPane {

    private static final double TYPING_PAUSE_SECONDS = 1;
    private static final KeyCombination SAVE = new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);
    private static final String ERROR_LINE_STYLE = "tex-error-line";

    private final Path texFile;
    private final Path cacheFolder;
    private final Supplier<String> preamble;
    private final Supplier<String> title;
    private final Optional<TectonicCompiler> compiler;
    private final CodeArea code;
    private final PdfView pdf = new PdfView();
    private final Label status = new Label();
    private final SplitPane split = new SplitPane();
    private final VirtualizedScrollPane<CodeArea> codeScroll;
    private final PauseTransition typingPause = new PauseTransition(Duration.seconds(TYPING_PAUSE_SECONDS));
    private final AtomicLong latestCompile = new AtomicLong();
    private boolean compiling;
    private boolean compileAgain;

    /** @param cacheFolder where the compiled PDF goes; it only leaves the cache on export (D-53) */
    public NoteEditorPane(Path texFile, Path cacheFolder, Supplier<String> preamble, Supplier<String> title,
                          Runnable onClose) throws IOException {
        this.texFile = texFile;
        this.preamble = preamble;
        this.title = title;
        this.cacheFolder = cacheFolder;
        this.compiler = TectonicCompiler.find();

        code = new CodeArea(NoteDocument.source(Files.readString(texFile, StandardCharsets.UTF_8)));
        codeScroll = new VirtualizedScrollPane<>(code);
        code.setParagraphGraphicFactory(LineNumberFactory.get(code));
        code.getStyleClass().add("tex-code");

        code.textProperty().addListener((o, a, text) -> {
            code.setStyleSpans(0, TexHighlighter.styles(text));
            status.setText("Editando…");
            typingPause.playFromStart();
        });
        code.setStyleSpans(0, TexHighlighter.styles(code.getText()));
        typingPause.setOnFinished(e -> {
            saveNow();
            status.setText("Salvo");
        });

        Button close = new Button("Fechar nota");
        close.setOnAction(e -> {
            saveNow();
            onClose.run();
        });
        Button compileButton = new Button("Compilar");
        compileButton.setOnAction(e -> saveAndCompile());
        addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (SAVE.match(e)) {
                saveAndCompile();
                e.consume();
            }
        });
        ToggleButton reading = new ToggleButton("Só leitura");
        reading.setOnAction(e -> setReading(reading.isSelected()));
        status.getStyleClass().add("label-text");
        HBox bar = new HBox(8, close, compileButton, reading, status);
        bar.setPadding(new Insets(6, 10, 6, 10));
        bar.getStyleClass().add("panel");

        split.setOrientation(Orientation.HORIZONTAL);
        split.getItems().addAll(codeScroll, pdf);
        setTop(bar);
        setCenter(split);

        Path existing = pdfPath();
        if (Files.exists(existing)) {
            pdf.show(existing);
        }
    }

    private void setReading(boolean reading) {
        if (reading) {
            split.getItems().setAll(pdf);
        } else {
            split.getItems().setAll(codeScroll, pdf);
        }
    }

    /** Writes the note. Called when closing, since the pause may not have ended. */
    public void saveNow() {
        typingPause.stop();
        try {
            String text = NoteDocument.compose(preamble.get(), title.get(), code.getText());
            if (!text.equals(Files.readString(texFile, StandardCharsets.UTF_8))) {
                Files.writeString(texFile, text, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            status.setText("Erro ao salvar: " + e.getMessage());
        }
    }

    private void saveAndCompile() {
        saveNow();
        compile();
    }

    private void compile() {
        if (compiler.isEmpty()) {
            status.setText("Salvo. Tectonic não encontrado, a nota não foi compilada.");
            return;
        }
        if (compiling) {
            compileAgain = true;
            return;
        }
        compiling = true;
        status.setText("Compilando…");
        long request = latestCompile.incrementAndGet();
        String content = NoteDocument.compose(preamble.get(), title.get(), code.getText());
        Thread.ofVirtual().start(() -> {
            TectonicCompiler.Result result;
            try {
                result = compiler.get().compile(texFile, content, cacheFolder);
            } catch (IOException | InterruptedException e) {
                result = new TectonicCompiler.Result(null, List.of(new TectonicCompiler.Problem(0, e.getMessage())), "");
            }
            TectonicCompiler.Result finished = result;
            Platform.runLater(() -> finish(finished, request));
        });
    }

    private void finish(TectonicCompiler.Result result, long request) {
        compiling = false;
        if (request == latestCompile.get()) {
            clearErrorLines();
            if (result.succeeded()) {
                pdf.show(result.pdf());
                status.setText("Compilado");
            } else {
                showProblems(result.problems());
            }
        }
        if (compileAgain) {
            compileAgain = false;
            compile();
        }
    }

    private void showProblems(List<TectonicCompiler.Problem> problems) {
        if (problems.isEmpty()) {
            status.setText("Erro na compilação.");
            return;
        }
        int header = NoteDocument.headerLines(preamble.get(), title.get());
        TectonicCompiler.Problem first = problems.getFirst();
        int firstLine = first.line() - header;
        status.setText(firstLine > 0
                ? "Erro na linha " + firstLine + ": " + first.message()
                : first.line() > 0 ? "Erro no preâmbulo da trilha: " + first.message() : "Erro: " + first.message());
        for (TectonicCompiler.Problem problem : problems) {
            int line = problem.line() - header;
            if (line > 0 && line <= lineCount()) {
                code.setParagraphStyle(line - 1, List.of(ERROR_LINE_STYLE));
            }
        }
    }

    private void clearErrorLines() {
        for (int i = 0; i < lineCount(); i++) {
            if (!code.getParagraph(i).getParagraphStyle().isEmpty()) {
                code.setParagraphStyle(i, List.of());
            }
        }
    }

    private int lineCount() {
        return (int) code.getText().chars().filter(c -> c == 10).count() + 1;
    }

    private Path pdfPath() {
        return cacheFolder.resolve(texFile.getFileName().toString().replaceFirst("\\.tex$", "") + ".pdf");
    }
}

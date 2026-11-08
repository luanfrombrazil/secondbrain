package io.github.luanfrombrazil.secondbrain.app;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.property.StateProperty;
import io.github.luanfrombrazil.secondbrain.track.Track;
import io.github.luanfrombrazil.secondbrain.vault.Vault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelEditorTest {

    @TempDir
    Path root;

    private Vault vault;
    private LevelEditor editor;

    @BeforeEach
    void setUp() throws IOException {
        vault = Vault.open(root);
        Track track = vault.createTrack("Trilha");
        editor = new LevelEditor(vault, track);
    }

    @AfterEach
    void tearDown() throws IOException {
        vault.close();
    }

    private static String state(Card card) {
        return card.property(StateProperty.class).orElseThrow().optionId();
    }

    private Set<String> dependenciesOnDisk(Card card) throws IOException {
        vault.close();
        vault = Vault.open(root);
        Card reloaded = vault.tracks().getFirst().cards().stream()
                .filter(c -> c.id().equals(card.id())).findFirst().orElseThrow();
        return reloaded.dependencies();
    }

    @Test
    void connectingBlocksTheDependentAndUndoBringsItBack() throws IOException {
        Card a = editor.createCard("A", new double[]{1, 2});
        Card b = editor.createCard("B", new double[]{3, 4});
        assertTrue(editor.connect(a.id(), b.id()));
        assertEquals("blocked", state(b));
        assertNull(a.fixedPoint());

        assertTrue(editor.undo());
        assertEquals("available", state(b));
        assertFalse(b.dependencies().contains(a.id()));
        assertEquals(1, a.fixedPoint()[0]);

        assertTrue(editor.redo());
        assertEquals("blocked", state(b));
        assertEquals(Set.of(a.id()), dependenciesOnDisk(b));
    }

    @Test
    void cycleIsRefusedAndLeavesNothingToUndo() throws IOException {
        Card a = editor.createCard("A", null);
        Card b = editor.createCard("B", null);
        editor.connect(a.id(), b.id());
        assertFalse(editor.connect(b.id(), a.id()));
        assertTrue(editor.undo());
        assertTrue(b.dependencies().isEmpty());
    }

    @Test
    void undoingCreationRemovesTheNoteAndTheCard() throws IOException {
        Card a = editor.createCard("A", null);
        Path note = root.resolve(a.notePath());
        assertTrue(Files.exists(note));
        editor.undo();
        assertFalse(Files.exists(note));
        assertTrue(editor.level().isEmpty());
        editor.redo();
        assertTrue(Files.exists(note));
        assertEquals(1, editor.level().size());
    }

    @Test
    void deletingACardFreesItsDependentsAndUndoRestoresEverything() throws IOException {
        Card a = editor.createCard("A", null);
        Card b = editor.createCard("B", null);
        editor.connect(a.id(), b.id());
        assertEquals("blocked", state(b));

        assertTrue(editor.deleteCard(a.id()));
        assertEquals("available", state(b));
        assertTrue(b.dependencies().isEmpty());

        editor.undo();
        assertEquals("blocked", state(b));
        assertEquals(Set.of(a.id()), b.dependencies());
        assertEquals(2, editor.level().size());
    }

    @Test
    void markingTasksMovesTheStateAndFreesDependents() throws IOException {
        Card a = editor.createCard("A", null);
        Card b = editor.createCard("B", null);
        editor.connect(a.id(), b.id());
        editor.addTask(a, "Lista 1");
        editor.addTask(a, "Lista 2");
        editor.setTaskDone(a, 0, true);
        assertEquals("in-progress", state(a));
        editor.setTaskDone(a, 1, true);
        assertEquals("done", state(a));
        assertEquals("available", state(b));
        editor.undo();
        assertEquals("in-progress", state(a));
        assertEquals("blocked", state(b));
    }

    @Test
    void stateChosenByHandSticksAndCanBeUndone() throws IOException {
        Card a = editor.createCard("A", null);
        Card b = editor.createCard("B", null);
        editor.connect(a.id(), b.id());
        editor.setState(b, "in-progress");
        assertEquals("in-progress", state(b));
        editor.undo();
        assertEquals("blocked", state(b));
        editor.setState(b, null);
        assertTrue(b.property(StateProperty.class).isEmpty());
        editor.undo();
        assertEquals("blocked", state(b));
    }

    @Test
    void titleTypeSelectionAndTextSurviveReopening() throws IOException {
        Card a = editor.createCard("A", null);
        var type = editor.createType("Disciplina");
        var selection = editor.createSelection("Prioridade", java.util.List.of("Alta", "Baixa"));
        editor.setTitle(a, "Novo");
        editor.setType(a, type.id());
        editor.setSelection(a, selection.id(), selection.options().get(1).id());
        editor.addText(a, "Nome oficial", "Cálculo");
        vault.close();
        vault = Vault.open(root);
        Track track = vault.tracks().getFirst();
        Card reloaded = track.cards().getFirst();
        assertEquals("Novo", reloaded.title());
        assertEquals(type.id(), reloaded.typeId());
        assertEquals("Baixa", track.selections().getFirst().options().get(1).name());
        assertEquals(selection.options().get(1).id(),
                reloaded.property(io.github.luanfrombrazil.secondbrain.property.SelectionProperty.class).orElseThrow().optionId());
        assertEquals("Cálculo",
                reloaded.property(io.github.luanfrombrazil.secondbrain.property.TextProperty.class).orElseThrow().value());
    }

    @Test
    void aNewEditClearsWhatCouldBeRedone() throws IOException {
        editor.createCard("A", null);
        editor.undo();
        editor.createCard("B", null);
        assertFalse(editor.redo());
    }

    @Test
    void innerCardsAreEditedInsideTheirParent() throws IOException {
        Card parent = editor.createCard("Pai", null);
        editor.open(parent);
        Card inner = editor.createCard("Filho", null);
        assertEquals(1, parent.innerCards().size());
        assertTrue(inner.notePath().contains("/pai/"));
    }
}

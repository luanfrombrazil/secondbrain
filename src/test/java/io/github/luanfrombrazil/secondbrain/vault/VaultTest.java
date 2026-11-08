package io.github.luanfrombrazil.secondbrain.vault;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.property.StateProperty;
import io.github.luanfrombrazil.secondbrain.property.TasksProperty;
import io.github.luanfrombrazil.secondbrain.track.Track;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VaultTest {

    @TempDir
    Path root;

    @Test
    void slugRemovesAccentsAndKeepsFileNamesSafe() {
        assertEquals("calculo-ii", Slug.of("Cálculo II"));
        assertEquals("calculo-i-limites", Slug.of("Cálculo I: limites"));
        assertEquals("con-nota", Slug.of("CON"));
        assertEquals("nota", Slug.of("???"));
        assertEquals("a-3", Slug.unique("A", Set.of("a", "a-2")::contains));
    }

    @Test
    void everythingSurvivesClosingAndOpeningAgain() throws IOException {
        String firstId;
        String secondId;
        try (Vault vault = Vault.open(root)) {
            Track track = vault.createTrack("Engenharia de Computação");
            Card first = vault.createCard(track, null, "Cálculo I");
            Card second = vault.createCard(track, null, "Cálculo II");
            second.dependencies().add(first.id());
            second.setFixedPoint(new double[]{10.5, -3});
            second.property(TasksProperty.class).orElseThrow().tasks().add(new TasksProperty.Task("Lista 1", true));
            vault.saveCard(track, second, null);
            firstId = first.id();
            secondId = second.id();
        }

        try (Vault vault = Vault.open(root)) {
            assertEquals(1, vault.tracks().size());
            Track track = vault.tracks().getFirst();
            assertEquals("Engenharia de Computação", track.name());
            assertEquals(4, track.states().size());
            assertEquals(2, track.cards().size());
            Card second = track.cards().stream().filter(c -> c.id().equals(secondId)).findFirst().orElseThrow();
            assertEquals("Cálculo II", second.title());
            assertEquals(Set.of(firstId), second.dependencies());
            assertArrayEquals(new double[]{10.5, -3}, second.fixedPoint());
            assertEquals("available", second.property(StateProperty.class).orElseThrow().optionId());
            assertEquals(1, second.property(TasksProperty.class).orElseThrow().doneCount());
        }
    }

    @Test
    void notesAreSluggedFilesAndInnerCardsLiveInTheCardFolder() throws IOException {
        try (Vault vault = Vault.open(root)) {
            Track track = vault.createTrack("Engenharia");
            Card parent = vault.createCard(track, null, "Cálculo II");
            Card repeated = vault.createCard(track, null, "Cálculo II");
            Card inner = vault.createCard(track, parent, "Limites");
            assertEquals("engenharia/calculo-ii.tex", parent.notePath());
            assertEquals("engenharia/calculo-ii-2.tex", repeated.notePath());
            assertEquals("engenharia/calculo-ii/limites.tex", inner.notePath());
            assertTrue(Files.exists(root.resolve(inner.notePath())));
        }
        try (Vault vault = Vault.open(root)) {
            Track track = vault.tracks().getFirst();
            assertEquals(2, track.cards().size());
            assertEquals(1, track.cards().stream().mapToInt(c -> c.innerCards().size()).sum());
        }
    }

    @Test
    void anEmptyNoteReceivesTheStartingTextButAWrittenOneIsKept() throws IOException {
        try (Vault vault = Vault.open(root)) {
            Track track = vault.createTrack("T");
            Card card = vault.createCard(track, null, "C");
            Path note = vault.notePath(card);
            Files.writeString(note, "");
            vault.fillIfEmpty(track, card);
            assertTrue(Files.readString(note).contains("documentclass"));
            Files.writeString(note, "meu texto");
            vault.fillIfEmpty(track, card);
            assertEquals("meu texto", Files.readString(note));
        }
    }

    @Test
    void savingAgainDoesNotLeaveTemporaryFiles() throws IOException {
        try (Vault vault = Vault.open(root)) {
            Track track = vault.createTrack("T");
            Card card = vault.createCard(track, null, "C");
            vault.saveCard(track, card, null);
            try (var files = Files.list(root.resolve(".secondbrain/cards"))) {
                assertFalse(files.anyMatch(f -> f.toString().endsWith(".tmp")));
            }
        }
    }

    @Test
    void aVaultCanOnlyBeOpenOnce() throws IOException {
        try (Vault vault = Vault.open(root)) {
            assertThrows(VaultInUseException.class, () -> Vault.open(root));
        }
    }
}

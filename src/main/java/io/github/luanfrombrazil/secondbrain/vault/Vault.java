package io.github.luanfrombrazil.secondbrain.vault;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.card.CardType;
import io.github.luanfrombrazil.secondbrain.property.ProgressProperty;
import io.github.luanfrombrazil.secondbrain.property.Properties;
import io.github.luanfrombrazil.secondbrain.property.Property;
import io.github.luanfrombrazil.secondbrain.property.StateProperty;
import io.github.luanfrombrazil.secondbrain.property.TasksProperty;
import io.github.luanfrombrazil.secondbrain.track.Selection;
import io.github.luanfrombrazil.secondbrain.track.StateOption;
import io.github.luanfrombrazil.secondbrain.track.Track;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * The folder with everything the user keeps. Notes stay as plain files; all the rest lives in
 * .secondbrain/, one small YAML file per card, each with a version for future migrations.
 */
public final class Vault implements AutoCloseable {

    private static final int VERSION = 1;
    private static final String APP_FOLDER = ".secondbrain";
    private static final String SETTINGS_FILE = "settings.yaml";

    private final Path root;
    private final FileChannel lockChannel;
    private final FileLock lock;
    private final List<Track> tracks = new ArrayList<>();

    private Vault(Path root, FileChannel lockChannel, FileLock lock) {
        this.root = root;
        this.lockChannel = lockChannel;
        this.lock = lock;
    }

    /** Opens a folder as a vault, creating .secondbrain/ if needed. Only one window per vault (D-63). */
    public static Vault open(Path root) throws IOException {
        Path app = root.resolve(APP_FOLDER);
        Files.createDirectories(app);
        FileChannel channel = FileChannel.open(app.resolve("lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        FileLock lock;
        try {
            lock = channel.tryLock();
        } catch (OverlappingFileLockException e) {
            lock = null;
        }
        if (lock == null) {
            channel.close();
            throw new VaultInUseException(root);
        }
        Vault vault = new Vault(root, channel, lock);
        vault.load();
        return vault;
    }

    public Path root() {
        return root;
    }

    public List<Track> tracks() {
        return tracks;
    }

    /** Creates a track with the default states and types, and its folder. */
    public Track createTrack(String name) throws IOException {
        String folder = Slug.unique(name, slug -> Files.exists(root.resolve(slug)));
        Files.createDirectories(root.resolve(folder));
        Track track = new Track(UUID.randomUUID().toString(), name, "#4f8fea", folder);
        track.states().add(new StateOption("blocked", "Bloqueado", "#e0524d", StateOption.Category.BLOCKED));
        track.states().add(new StateOption("available", "Disponível", "#e7b53c", StateOption.Category.AVAILABLE));
        track.states().add(new StateOption("in-progress", "Em andamento", "#4f8fea", StateOption.Category.IN_PROGRESS));
        track.states().add(new StateOption("done", "Concluído", "#47b26b", StateOption.Category.DONE));
        tracks.add(track);
        saveTrack(track);
        return track;
    }

    /**
     * Creates a card and its empty note.
     *
     * @param parent the card whose inner graph receives the new one, or null for the track's root
     */
    public Card createCard(Track track, Card parent, String title) throws IOException {
        Path folder = parent == null
                ? root.resolve(track.folder())
                : root.resolve(withoutExtension(parent.notePath()));
        Files.createDirectories(folder);
        String slug = Slug.unique(title, s -> Files.exists(folder.resolve(s + ".tex")));
        Path note = folder.resolve(slug + ".tex");
        Files.writeString(note, noteTemplate(track, title), StandardCharsets.UTF_8);

        Card card = new Card(UUID.randomUUID().toString(), title, root.relativize(note).toString().replace('\\', '/'));
        track.firstOption(StateOption.Category.AVAILABLE)
                .ifPresent(option -> card.properties().add(new StateProperty(option.id())));
        card.properties().add(new TasksProperty());
        card.properties().add(new ProgressProperty());
        (parent == null ? track.cards() : parent.innerCards()).add(card);
        saveCard(track, card, parent);
        return card;
    }

    public void saveTrack(Track track) throws IOException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", VERSION);
        data.put("id", track.id());
        data.put("name", track.name());
        data.put("color", track.color());
        data.put("folder", track.folder());
        data.put("preamble", track.preamble());
        data.put("states", track.states().stream().map(Vault::optionData).toList());
        data.put("types", track.types().stream().map(Vault::typeData).toList());
        data.put("selections", track.selections().stream().map(Vault::selectionData).toList());
        YamlFiles.write(trackFile(track.id()), data);
    }

    public void saveCard(Track track, Card card, Card parent) throws IOException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", VERSION);
        data.put("id", card.id());
        data.put("track", track.id());
        if (parent != null) {
            data.put("parent", parent.id());
        }
        data.put("title", card.title());
        data.put("note", card.notePath());
        if (card.typeId() != null) {
            data.put("type", card.typeId());
        }
        data.put("dependencies", new ArrayList<>(card.dependencies()));
        data.put("links", new ArrayList<>(card.links()));
        if (card.fixedPoint() != null) {
            data.put("fixed", List.of(card.fixedPoint()[0], card.fixedPoint()[1]));
        }
        List<Map<String, Object>> properties = new ArrayList<>();
        for (Property property : card.properties()) {
            Map<String, Object> entry = new LinkedHashMap<>(property.toData());
            entry.put("kind", property.kind());
            properties.add(entry);
        }
        data.put("properties", properties);
        YamlFiles.write(cardFile(card.id()), data);
    }

    /**
     * Removes a card with no inner graph: the note goes to the system trash (D-34).
     *
     * @return the note's content, for undoing
     */
    public byte[] deleteCard(Track track, Card parent, Card card) throws IOException {
        if (!card.innerCards().isEmpty()) {
            throw new IOException("Card has an inner graph: " + card.id());
        }
        Path note = root.resolve(card.notePath());
        byte[] content = Files.exists(note) ? Files.readAllBytes(note) : new byte[0];
        boolean untouched = new String(content, StandardCharsets.UTF_8).equals(noteTemplate(track, card.title()));
        moveToTrash(note, untouched ? 0 : content.length);
        level(track, parent).remove(card);
        Files.deleteIfExists(cardFile(card.id()));
        return content;
    }

    private static String noteTemplate(Track track, String title) {
        return track.preamble() + "\n\\title{" + title + "}\n\\author{}\n\n\\begin{document}\n\\maketitle\n\n\n\\end{document}\n";
    }

    /** Gives an empty note the track's starting text, so that it can compile. */
    public void fillIfEmpty(Track track, Card card) throws IOException {
        Path note = notePath(card);
        if (Files.exists(note) && Files.size(note) == 0) {
            Files.writeString(note, noteTemplate(track, card.title()), StandardCharsets.UTF_8);
        }
    }

    public Path notePath(Card card) {
        return root.resolve(card.notePath());
    }

    /** Where this card's compiled PDF lives. It only leaves the cache on export (D-53). */
    public Path cacheFolder(Card card) {
        return root.resolve(APP_FOLDER).resolve("cache").resolve(card.id());
    }

    public Path pdfPath(Card card) {
        String name = Path.of(card.notePath()).getFileName().toString().replaceFirst("\\.tex$", "") + ".pdf";
        return cacheFolder(card).resolve(name);
    }

    /** Puts a deleted card back, with its note. */
    public void restoreCard(Track track, Card parent, Card card, byte[] noteContent) throws IOException {
        Path note = root.resolve(card.notePath());
        Files.createDirectories(note.getParent());
        Files.write(note, noteContent);
        level(track, parent).add(card);
        saveCard(track, card, parent);
    }

    /** Saves these cards, wherever they are in the track. */
    public void saveCards(Track track, Collection<Card> cards) throws IOException {
        Map<Card, Card> parents = new HashMap<>();
        collectParents(track.cards(), null, parents);
        for (Card card : cards) {
            saveCard(track, card, parents.get(card));
        }
    }

    public Map<String, Object> readSettings() throws IOException {
        Path file = root.resolve(APP_FOLDER).resolve(SETTINGS_FILE);
        return Files.exists(file) ? YamlFiles.read(file) : Map.of();
    }

    public void saveSettings(Map<String, Object> settings) throws IOException {
        Map<String, Object> data = new LinkedHashMap<>(settings);
        data.put("version", VERSION);
        YamlFiles.write(root.resolve(APP_FOLDER).resolve(SETTINGS_FILE), data);
    }

    private static void collectParents(List<Card> level, Card parent, Map<Card, Card> parents) {
        for (Card card : level) {
            parents.put(card, parent);
            collectParents(card.innerCards(), card, parents);
        }
    }

    private static List<Card> level(Track track, Card parent) {
        return parent == null ? track.cards() : parent.innerCards();
    }

    /** An empty note holds nothing to recover, so it can simply go. */
    private static void moveToTrash(Path note, long size) throws IOException {
        if (!Files.exists(note)) {
            return;
        }
        if (size == 0) {
            Files.delete(note);
            return;
        }
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.MOVE_TO_TRASH)
                || !Desktop.getDesktop().moveToTrash(note.toFile())) {
            throw new IOException("Could not move to the trash: " + note);
        }
    }

    @Override
    public void close() throws IOException {
        lock.release();
        lockChannel.close();
    }

    private void load() throws IOException {
        Map<String, Track> byId = new HashMap<>();
        for (Path file : yamlFiles("tracks")) {
            Track track = readTrack(YamlFiles.read(file));
            tracks.add(track);
            byId.put(track.id(), track);
        }
        Map<String, Card> cards = new HashMap<>();
        Map<String, Map<String, Object>> cardData = new LinkedHashMap<>();
        for (Path file : yamlFiles("cards")) {
            Map<String, Object> data = YamlFiles.read(file);
            Card card = readCard(data);
            cards.put(card.id(), card);
            cardData.put(card.id(), data);
        }
        cardData.forEach((id, data) -> {
            Card parent = cards.get((String) data.get("parent"));
            Track track = byId.get((String) data.get("track"));
            if (parent != null) {
                parent.innerCards().add(cards.get(id));
            } else if (track != null) {
                track.cards().add(cards.get(id));
            }
        });
    }

    private List<Path> yamlFiles(String folder) throws IOException {
        Path directory = root.resolve(APP_FOLDER).resolve(folder);
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(f -> f.toString().endsWith(".yaml")).sorted().toList();
        }
    }

    @SuppressWarnings("unchecked")
    private static Track readTrack(Map<String, Object> data) {
        Track track = new Track((String) data.get("id"), (String) data.get("name"),
                (String) data.get("color"), (String) data.get("folder"));
        if (data.get("preamble") instanceof String preamble) {
            track.setPreamble(preamble);
        }
        for (Map<String, Object> state : (List<Map<String, Object>>) data.getOrDefault("states", List.of())) {
            track.states().add(new StateOption((String) state.get("id"), (String) state.get("name"),
                    (String) state.get("color"), StateOption.Category.valueOf((String) state.get("category"))));
        }
        for (Map<String, Object> type : (List<Map<String, Object>>) data.getOrDefault("types", List.of())) {
            track.types().add(new CardType((String) type.get("id"), (String) type.get("name"),
                    (String) type.get("icon"), (List<String>) type.get("properties")));
        }
        for (Map<String, Object> selection : (List<Map<String, Object>>) data.getOrDefault("selections", List.of())) {
            List<Selection.Option> options = new ArrayList<>();
            for (Map<String, Object> option : (List<Map<String, Object>>) selection.get("options")) {
                options.add(new Selection.Option((String) option.get("id"), (String) option.get("name")));
            }
            track.selections().add(new Selection((String) selection.get("id"), (String) selection.get("name"), options));
        }
        return track;
    }

    @SuppressWarnings("unchecked")
    private static Card readCard(Map<String, Object> data) {
        Card card = new Card((String) data.get("id"), (String) data.get("title"), (String) data.get("note"));
        card.setTypeId((String) data.get("type"));
        card.dependencies().addAll((List<String>) data.getOrDefault("dependencies", List.of()));
        card.links().addAll((List<String>) data.getOrDefault("links", List.of()));
        List<Number> fixed = (List<Number>) data.get("fixed");
        if (fixed != null) {
            card.setFixedPoint(new double[]{fixed.get(0).doubleValue(), fixed.get(1).doubleValue()});
        }
        for (Map<String, Object> entry : (List<Map<String, Object>>) data.getOrDefault("properties", List.of())) {
            Properties.fromData((String) entry.get("kind"), entry).ifPresent(card.properties()::add);
        }
        return card;
    }

    private static Map<String, Object> optionData(StateOption option) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", option.id());
        data.put("name", option.name());
        data.put("color", option.color());
        data.put("category", option.category().name());
        return data;
    }

    private static Map<String, Object> typeData(CardType type) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", type.id());
        data.put("name", type.name());
        data.put("icon", type.icon());
        data.put("properties", type.initialProperties());
        return data;
    }

    private static Map<String, Object> selectionData(Selection selection) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", selection.id());
        data.put("name", selection.name());
        data.put("options", selection.options().stream()
                .map(o -> Map.<String, Object>of("id", o.id(), "name", o.name())).toList());
        return data;
    }

    private static String withoutExtension(String path) {
        int dot = path.lastIndexOf('.');
        return dot < 0 ? path : path.substring(0, dot);
    }

    private Path trackFile(String id) {
        return root.resolve(APP_FOLDER).resolve("tracks").resolve(id + ".yaml");
    }

    private Path cardFile(String id) {
        return root.resolve(APP_FOLDER).resolve("cards").resolve(id + ".yaml");
    }
}

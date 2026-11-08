package io.github.luanfrombrazil.secondbrain.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** A checklist. Tasks never have dates (D-37). */
public final class TasksProperty implements Property {

    public static final String KIND = "tasks";

    public record Task(String text, boolean done) {
    }

    private final List<Task> tasks = new ArrayList<>();

    @SuppressWarnings("unchecked")
    static TasksProperty fromData(Map<String, Object> data) {
        TasksProperty property = new TasksProperty();
        for (Map<String, Object> item : (List<Map<String, Object>>) data.getOrDefault("items", List.of())) {
            property.tasks.add(new Task((String) item.get("text"), Boolean.TRUE.equals(item.get("done"))));
        }
        return property;
    }

    public List<Task> tasks() {
        return tasks;
    }

    public int doneCount() {
        return (int) tasks.stream().filter(Task::done).count();
    }

    @Override
    public String kind() {
        return KIND;
    }

    @Override
    public Map<String, Object> toData() {
        List<Map<String, Object>> items = tasks.stream()
                .map(t -> Map.<String, Object>of("text", t.text(), "done", t.done()))
                .toList();
        return Map.of("items", items);
    }
}

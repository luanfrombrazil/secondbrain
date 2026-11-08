package io.github.luanfrombrazil.secondbrain.graph;

/** Joins two nodes. In a connection, the source is the prerequisite and the target is the dependent. */
public record Edge(String source, String target) {
}

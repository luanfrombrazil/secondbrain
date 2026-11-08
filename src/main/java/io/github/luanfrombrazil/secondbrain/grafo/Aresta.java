package io.github.luanfrombrazil.secondbrain.grafo;

/** Liga dois nós. Numa conexão, a origem é o pré-requisito e o destino é o dependente. */
public record Aresta(String origem, String destino) {
}

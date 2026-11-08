package io.github.luanfrombrazil.secondbrain.grafo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Os nós com conexão, organizados em árvores. Cada nó tem um pai principal, escolhido na camada
 * logo anterior. As outras dependências continuam sendo conexões, mas não decidem a posição.
 */
final class Floresta {

    static final Comparator<String> SEM_PASTA_POR_ULTIMO =
            Comparator.nullsLast(Comparator.naturalOrder());

    final List<String> raizes = new ArrayList<>();
    final Map<String, List<String>> filhos = new HashMap<>();
    private final Map<String, Integer> folhas = new HashMap<>();

    Floresta(Grafo grafo) {
        Comparator<String> ordem = Comparator
                .comparing((String id) -> grafo.no(id).pasta(), SEM_PASTA_POR_ULTIMO)
                .thenComparing(Comparator.naturalOrder());
        for (No no : grafo.nos()) {
            if (grafo.solto(no.id())) {
                continue;
            }
            filhos.putIfAbsent(no.id(), new ArrayList<>());
            int camada = grafo.camada(no.id());
            if (camada == 0) {
                raizes.add(no.id());
                continue;
            }
            String pai = grafo.dependenciasDe(no.id()).stream()
                    .filter(d -> grafo.camada(d) == camada - 1)
                    .findFirst().orElseThrow();
            filhos.computeIfAbsent(pai, p -> new ArrayList<>()).add(no.id());
        }
        raizes.sort(ordem);
        filhos.values().forEach(lista -> lista.sort(ordem));
        raizes.forEach(this::contarFolhas);
    }

    private int contarFolhas(String id) {
        int total = filhos.get(id).stream().mapToInt(this::contarFolhas).sum();
        int resultado = Math.max(1, total);
        folhas.put(id, resultado);
        return resultado;
    }

    int folhas(String id) {
        return folhas.get(id);
    }
}

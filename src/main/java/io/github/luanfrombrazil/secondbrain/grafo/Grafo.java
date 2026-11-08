package io.github.luanfrombrazil.secondbrain.grafo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A entrada das disposições: nós, conexões (dependências, sem ciclo) e links.
 * É imutável e não sabe nada de tela nem de arquivos.
 */
public final class Grafo {

    private final Map<String, No> nos = new LinkedHashMap<>();
    private final List<Aresta> conexoes;
    private final List<Aresta> links;
    private final Map<String, List<String>> dependencias = new HashMap<>();
    private final Map<String, List<String>> dependentes = new HashMap<>();
    private final Map<String, Integer> camadas = new HashMap<>();

    public Grafo(Collection<No> nos, List<Aresta> conexoes, List<Aresta> links) {
        nos.forEach(no -> this.nos.put(no.id(), no));
        this.conexoes = List.copyOf(conexoes);
        this.links = List.copyOf(links);
        for (No no : nos) {
            dependencias.put(no.id(), new ArrayList<>());
            dependentes.put(no.id(), new ArrayList<>());
        }
        for (Aresta a : conexoes) {
            exigir(a.origem());
            exigir(a.destino());
            dependencias.get(a.destino()).add(a.origem());
            dependentes.get(a.origem()).add(a.destino());
        }
        links.forEach(a -> {
            exigir(a.origem());
            exigir(a.destino());
        });
        calcularCamadas();
    }

    private void exigir(String id) {
        if (!nos.containsKey(id)) {
            throw new IllegalArgumentException("Nó desconhecido: " + id);
        }
    }

    /** Caminho mais longo a partir das raízes (Kahn): cada nó fica depois de tudo de que depende. */
    private void calcularCamadas() {
        Map<String, Integer> faltam = new HashMap<>();
        ArrayDeque<String> fila = new ArrayDeque<>();
        for (String id : nos.keySet()) {
            faltam.put(id, dependencias.get(id).size());
            camadas.put(id, 0);
            if (dependencias.get(id).isEmpty()) {
                fila.add(id);
            }
        }
        int vistos = 0;
        while (!fila.isEmpty()) {
            String id = fila.poll();
            vistos++;
            for (String filho : dependentes.get(id)) {
                camadas.merge(filho, camadas.get(id) + 1, Math::max);
                if (faltam.merge(filho, -1, Integer::sum) == 0) {
                    fila.add(filho);
                }
            }
        }
        if (vistos != nos.size()) {
            throw new IllegalArgumentException("As conexões formam um ciclo");
        }
    }

    public Collection<No> nos() {
        return nos.values();
    }

    public No no(String id) {
        return nos.get(id);
    }

    public List<Aresta> conexoes() {
        return conexoes;
    }

    public List<Aresta> links() {
        return links;
    }

    public List<String> dependenciasDe(String id) {
        return dependencias.get(id);
    }

    public List<String> dependentesDe(String id) {
        return dependentes.get(id);
    }

    public int camada(String id) {
        return camadas.get(id);
    }

    /** Solto é o nó sem nenhuma conexão. Link não conta. */
    public boolean solto(String id) {
        return dependencias.get(id).isEmpty() && dependentes.get(id).isEmpty();
    }

    /** O mesmo grafo com os cards soltos liberados do ponto onde foram criados. */
    public Grafo semFixos() {
        return new Grafo(nos.values().stream().map(No::semFixo).toList(), conexoes, links);
    }

    public Grafo comNo(No no) {
        List<No> novos = new ArrayList<>(nos.values());
        novos.add(no);
        return new Grafo(novos, conexoes, links);
    }
}

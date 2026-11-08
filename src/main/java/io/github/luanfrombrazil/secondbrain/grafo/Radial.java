package io.github.luanfrombrazil.secondbrain.grafo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Anéis por nível de dependência. Cada subárvore recebe uma fatia proporcional às suas folhas.
 * Com várias raízes, elas formam o primeiro anel em volta de um centro vazio.
 */
public final class Radial extends DisposicaoEmArvore {

    private static final double SEPARACAO = Math.hypot(No.LARGURA, No.ALTURA) + 6;

    @Override
    protected Map<String, Ponto> arrumar(Grafo grafo, Floresta f, Parametros p) {
        int deslocamento = f.raizes.size() == 1 ? 0 : 1;
        TreeMap<Integer, List<String>> aneis = new TreeMap<>();
        f.filhos.keySet().forEach(id ->
                aneis.computeIfAbsent(grafo.camada(id) + deslocamento, k -> new ArrayList<>()).add(id));

        double passo = p.passoRadial;
        for (var anel : aneis.entrySet()) {
            if (anel.getKey() > 0) {
                passo = Math.max(passo, anel.getValue().size() * SEPARACAO / (2 * Math.PI * anel.getKey()));
            }
        }

        Map<String, Double> angulos = new HashMap<>();
        double total = f.raizes.stream().mapToInt(f::folhas).sum();
        double cursor = 0;
        for (String raiz : f.raizes) {
            double fim = cursor + 2 * Math.PI * f.folhas(raiz) / total;
            fatiar(raiz, cursor, fim, f, angulos);
            cursor = fim;
        }

        // Quem tem mais de um pai fica no ângulo entre eles.
        for (var anel : aneis.entrySet()) {
            for (String id : anel.getValue()) {
                if (grafo.dependenciasDe(id).size() > 1) {
                    angulos.put(id, mediaCircular(grafo.dependenciasDe(id), angulos));
                }
            }
        }

        Map<String, Ponto> posicoes = new HashMap<>();
        for (var anel : aneis.entrySet()) {
            double raio = anel.getKey() * passo;
            if (raio > 0) {
                espalhar(anel.getValue(), angulos, SEPARACAO / raio);
            }
            for (String id : anel.getValue()) {
                double angulo = angulos.get(id);
                posicoes.put(id, new Ponto(raio * Math.cos(angulo), raio * Math.sin(angulo)));
            }
        }
        return posicoes;
    }

    private void fatiar(String id, double inicio, double fim, Floresta f, Map<String, Double> angulos) {
        angulos.put(id, (inicio + fim) / 2);
        double cursor = inicio;
        for (String filho : f.filhos.get(id)) {
            double parte = (fim - inicio) * f.folhas(filho) / f.folhas(id);
            fatiar(filho, cursor, cursor + parte, f, angulos);
            cursor += parte;
        }
    }

    private double mediaCircular(List<String> ids, Map<String, Double> angulos) {
        double x = ids.stream().mapToDouble(id -> Math.cos(angulos.get(id))).sum();
        double y = ids.stream().mapToDouble(id -> Math.sin(angulos.get(id))).sum();
        return Math.atan2(y, x);
    }

    /** Garante a distância mínima entre vizinhos do mesmo anel. O passo já garante que cabem. */
    private void espalhar(List<String> anel, Map<String, Double> angulos, double minimo) {
        anel.sort(Comparator.comparingDouble(angulos::get));
        for (int i = 1; i < anel.size(); i++) {
            angulos.merge(anel.get(i), angulos.get(anel.get(i - 1)) + minimo, Math::max);
        }
        double limite = angulos.get(anel.getFirst()) + 2 * Math.PI - minimo;
        for (int i = anel.size() - 1; i > 0; i--) {
            angulos.merge(anel.get(i), limite, Math::min);
            limite = angulos.get(anel.get(i)) - minimo;
        }
    }
}

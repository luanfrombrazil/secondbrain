package io.github.luanfrombrazil.secondbrain.grafo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisposicoesTest {

    private final Parametros p = new Parametros();

    private static Grafo grafo(List<String> ids, String... arestas) {
        List<No> nos = ids.stream().map(id -> new No(id, null, null)).toList();
        List<Aresta> lista = new ArrayList<>();
        for (String a : arestas) {
            String[] partes = a.split(">");
            lista.add(new Aresta(partes[0], partes[1]));
        }
        return new Grafo(nos, lista, List.of());
    }

    private static Grafo aleatorioSemCiclo(int n) {
        java.util.Random sorte = new java.util.Random(7);
        List<No> nos = new ArrayList<>();
        List<Aresta> arestas = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            nos.add(new No("n" + i, "p" + i % 4, null));
            if (i > 0 && i % 10 != 0) {
                int pai = sorte.nextInt(i);
                arestas.add(new Aresta("n" + pai, "n" + i));
                if (i > 3 && sorte.nextBoolean()) {
                    int outro = sorte.nextInt(i);
                    if (outro != pai) {
                        arestas.add(new Aresta("n" + outro, "n" + i));
                    }
                }
            }
        }
        return new Grafo(nos, arestas, List.of());
    }

    private static void semSobreposicao(Map<String, Ponto> posicoes) {
        List<Map.Entry<String, Ponto>> lista = new ArrayList<>(posicoes.entrySet());
        for (int i = 0; i < lista.size(); i++) {
            for (int j = i + 1; j < lista.size(); j++) {
                Ponto a = lista.get(i).getValue();
                Ponto b = lista.get(j).getValue();
                boolean sobrepoe = Math.abs(a.x() - b.x()) < No.LARGURA - 1e-6
                        && Math.abs(a.y() - b.y()) < No.ALTURA - 1e-6;
                assertFalse(sobrepoe, lista.get(i).getKey() + " em cima de " + lista.get(j).getKey());
            }
        }
    }

    @Test
    void cicloERecusado() {
        assertThrows(IllegalArgumentException.class, () -> grafo(List.of("a", "b", "c"), "a>b", "b>c", "c>a"));
    }

    @Test
    void cadaCardFicaADireitaDoQueDepende() {
        Grafo g = aleatorioSemCiclo(200);
        Map<String, Ponto> pos = new PorDependencia().calcular(g, p);
        for (Aresta a : g.conexoes()) {
            assertTrue(pos.get(a.origem()).x() < pos.get(a.destino()).x());
        }
        semSobreposicao(pos);
    }

    @Test
    void cardQueEsperaFicaDepoisDeTodosOsPreRequisitos() {
        Grafo g = grafo(List.of("a", "b", "c", "d"), "a>b", "b>c", "a>d", "c>d");
        assertEquals(3, g.camada("d"));
    }

    @Test
    void filhoUnicoFicaNaMesmaLinhaDoPai() {
        Map<String, Ponto> pos = new PorDependencia().calcular(grafo(List.of("a", "b", "c"), "a>b", "b>c"), p);
        assertEquals(pos.get("a").y(), pos.get("b").y(), 1e-9);
        assertEquals(pos.get("b").y(), pos.get("c").y(), 1e-9);
    }

    @Test
    void doisFilhosFicamCentradosNoPai() {
        Map<String, Ponto> pos = new PorDependencia().calcular(grafo(List.of("a", "b", "c"), "a>b", "a>c"), p);
        assertEquals(pos.get("a").y(), (pos.get("b").y() + pos.get("c").y()) / 2, 1e-9);
        assertTrue(pos.get("b").y() != pos.get("c").y());
    }

    @Test
    void trezFilhosTambemFicamCentrados() {
        Map<String, Ponto> pos = new PorDependencia().calcular(grafo(List.of("a", "b", "c", "d"), "a>b", "a>c", "a>d"), p);
        double menor = Math.min(pos.get("b").y(), Math.min(pos.get("c").y(), pos.get("d").y()));
        double maior = Math.max(pos.get("b").y(), Math.max(pos.get("c").y(), pos.get("d").y()));
        assertEquals(pos.get("a").y(), (menor + maior) / 2, 1e-9);
    }

    @Test
    void soltoSemPontoVaiParaAFaixaAbaixoDaArvore() {
        Map<String, Ponto> pos = new PorDependencia().calcular(grafo(List.of("a", "b", "solto"), "a>b"), p);
        assertTrue(pos.get("solto").y() > pos.get("a").y() + No.ALTURA);
    }

    @Test
    void soltoFixoFicaOndeFoiCriadoEEmpurraOsVizinhos() {
        Grafo base = grafo(List.of("a", "b"), "a>b");
        Map<String, Ponto> antes = new PorDependencia().calcular(base, p);
        Ponto ponto = antes.get("a");
        Grafo comSolto = base.comNo(new No("solto", null, ponto));
        Map<String, Ponto> depois = new PorDependencia().calcular(comSolto, p);
        assertEquals(ponto, depois.get("solto"));
        semSobreposicao(depois);
    }

    @Test
    void radialPoeCadaNivelNoMesmoRaio() {
        Grafo g = grafo(List.of("r", "a", "b", "c", "d"), "r>a", "r>b", "a>c", "b>d");
        Map<String, Ponto> pos = new Radial().calcular(g, p);
        assertEquals(0, Math.hypot(pos.get("r").x(), pos.get("r").y()), 1e-9);
        assertEquals(Math.hypot(pos.get("a").x(), pos.get("a").y()), Math.hypot(pos.get("b").x(), pos.get("b").y()), 1e-6);
        assertEquals(Math.hypot(pos.get("c").x(), pos.get("c").y()), Math.hypot(pos.get("d").x(), pos.get("d").y()), 1e-6);
        semSobreposicao(pos);
    }

    @Test
    void radialNaoSobrepoeComGrafoGrande() {
        semSobreposicao(new Radial().calcular(aleatorioSemCiclo(200), p));
    }

    @Test
    void forcasSaoDeterministicasENaoSobrepoem() {
        Grafo g = aleatorioSemCiclo(100);
        Map<String, Ponto> a = new PorForcas().calcular(g, p);
        Map<String, Ponto> b = new PorForcas().calcular(g, p);
        assertEquals(a, b);
        semSobreposicao(a);
    }

    @Test
    void forcasNaoMexemNoFixo() {
        Grafo g = grafo(List.of("a", "b"), "a>b").comNo(new No("solto", null, new Ponto(50, 50)));
        assertEquals(new Ponto(50, 50), new PorForcas().calcular(g, p).get("solto"));
    }
}

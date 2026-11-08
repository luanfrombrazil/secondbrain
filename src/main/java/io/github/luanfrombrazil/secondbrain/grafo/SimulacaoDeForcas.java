package io.github.luanfrombrazil.secondbrain.grafo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Os cards se repelem (Barnes-Hut), conexões e links puxam como molas, e o sistema esfria até parar.
 * Com o mesmo grafo e os mesmos parâmetros, o resultado é sempre o mesmo.
 */
public final class SimulacaoDeForcas {

    private static final double THETA = 0.8;
    private static final double ALFA_MINIMO = 0.002;
    private static final double FORCA_DO_LINK = 0.35;
    private static final double MASSA_DO_FIXO = 3;
    private static final double FOLGA = 16;

    private final Parametros p;
    private final String[] ids;
    private final double[] x;
    private final double[] y;
    private final double[] vx;
    private final double[] vy;
    private final boolean[] preso;
    private final double[] massa;
    private final int[][] conexoes;
    private final int[][] links;
    private final Map<String, List<Integer>> pastas = new HashMap<>();
    private double alfa = 1;

    public SimulacaoDeForcas(Grafo grafo, Parametros p, Map<String, Ponto> inicio) {
        this.p = p;
        ids = grafo.nos().stream().map(No::id).sorted().toArray(String[]::new);
        int n = ids.length;
        x = new double[n];
        y = new double[n];
        vx = new double[n];
        vy = new double[n];
        preso = new boolean[n];
        massa = new double[n];
        Map<String, Integer> indice = new HashMap<>();
        for (int i = 0; i < n; i++) {
            indice.put(ids[i], i);
            No no = grafo.no(ids[i]);
            Ponto ponto = no.fixo() != null && grafo.solto(no.id()) ? no.fixo() : inicio.get(ids[i]);
            preso[i] = no.fixo() != null && grafo.solto(no.id());
            massa[i] = preso[i] ? MASSA_DO_FIXO : 1;
            if (ponto == null) {
                // Espiral de girassol: espalha sem sorteio.
                double raio = 300 * Math.sqrt(i + 0.5);
                double angulo = i * Math.PI * (3 - Math.sqrt(5));
                ponto = new Ponto(raio * Math.cos(angulo), raio * Math.sin(angulo));
            }
            x[i] = ponto.x();
            y[i] = ponto.y();
            if (no.pasta() != null) {
                pastas.computeIfAbsent(no.pasta(), k -> new ArrayList<>()).add(i);
            }
        }
        conexoes = pares(grafo.conexoes(), indice);
        links = pares(grafo.links(), indice);
    }

    private static int[][] pares(List<Aresta> arestas, Map<String, Integer> indice) {
        return arestas.stream()
                .map(a -> new int[]{indice.get(a.origem()), indice.get(a.destino())})
                .toArray(int[][]::new);
    }

    /** Avança um quadro. Devolve falso quando o sistema já assentou. */
    public boolean passo() {
        if (alfa < ALFA_MINIMO) {
            return false;
        }
        repelir();
        puxar(conexoes, p.mola);
        puxar(links, p.mola * FORCA_DO_LINK);
        agruparPastas();
        for (int i = 0; i < ids.length; i++) {
            if (preso[i]) {
                vx[i] = 0;
                vy[i] = 0;
                continue;
            }
            vx[i] = (vx[i] - x[i] * p.gravidade * alfa) * p.atrito;
            vy[i] = (vy[i] - y[i] * p.gravidade * alfa) * p.atrito;
            x[i] += vx[i];
            y[i] += vy[i];
        }
        separarEncostados();
        alfa *= 1 - p.resfriamento;
        return true;
    }

    public void reaquecer() {
        alfa = Math.max(alfa, 0.6);
    }

    public Map<String, Ponto> posicoes() {
        Map<String, Ponto> saida = new HashMap<>();
        for (int i = 0; i < ids.length; i++) {
            saida.put(ids[i], new Ponto(x[i], y[i]));
        }
        return saida;
    }

    private void repelir() {
        int n = ids.length;
        if (n < 2) {
            return;
        }
        double minX = Arrays.stream(x).min().orElse(0);
        double maxX = Arrays.stream(x).max().orElse(0);
        double minY = Arrays.stream(y).min().orElse(0);
        double maxY = Arrays.stream(y).max().orElse(0);
        double meia = Math.max(maxX - minX, maxY - minY) / 2 + 1;
        Celula raiz = new Celula((minX + maxX) / 2, (minY + maxY) / 2, meia);
        for (int i = 0; i < n; i++) {
            raiz.inserir(i, x, y, massa, 0);
        }
        for (int i = 0; i < n; i++) {
            if (!preso[i]) {
                raiz.aplicar(i, x, y, vx, vy, p.repulsao * alfa);
            }
        }
    }

    private void puxar(int[][] pares, double forca) {
        for (int[] par : pares) {
            int a = par[0];
            int b = par[1];
            double dx = x[b] - x[a];
            double dy = y[b] - y[a];
            double d = Math.max(1, Math.hypot(dx, dy));
            double k = (d - p.comprimentoMola) / d * forca * alfa;
            vx[a] += dx * k;
            vy[a] += dy * k;
            vx[b] -= dx * k;
            vy[b] -= dy * k;
        }
    }

    private void agruparPastas() {
        for (List<Integer> pasta : pastas.values()) {
            double cx = pasta.stream().mapToDouble(i -> x[i]).average().orElse(0);
            double cy = pasta.stream().mapToDouble(i -> y[i]).average().orElse(0);
            for (int i : pasta) {
                vx[i] += (cx - x[i]) * p.forcaPasta * alfa;
                vy[i] += (cy - y[i]) * p.forcaPasta * alfa;
            }
        }
    }

    /** Varredura pelo eixo x: só compara cards que podem estar se encostando. */
    private void separarEncostados() {
        double minX = No.LARGURA + FOLGA;
        double minY = No.ALTURA + FOLGA;
        for (int rodada = 0; rodada < 3; rodada++) {
            Integer[] ordem = IntStream.range(0, ids.length).boxed().toArray(Integer[]::new);
            Arrays.sort(ordem, Comparator.comparingDouble(i -> x[i]));
            for (int ia = 0; ia < ordem.length; ia++) {
                int a = ordem[ia];
                for (int ib = ia + 1; ib < ordem.length && x[ordem[ib]] - x[a] < minX; ib++) {
                    separar(a, ordem[ib], minX, minY);
                }
            }
        }
    }

    private void separar(int a, int b, double minX, double minY) {
        double dx = x[b] - x[a];
        double dy = y[b] - y[a];
        double sobraX = minX - Math.abs(dx);
        double sobraY = minY - Math.abs(dy);
        if (sobraX <= 0 || sobraY <= 0) {
            return;
        }
        double parteA = preso[a] ? 0 : preso[b] ? 1 : 0.5;
        double parteB = preso[b] ? 0 : preso[a] ? 1 : 0.5;
        if (sobraY < sobraX) {
            double sinal = dy >= 0 ? 1 : -1;
            y[a] -= sinal * sobraY * parteA;
            y[b] += sinal * sobraY * parteB;
        } else {
            double sinal = dx >= 0 ? 1 : -1;
            x[a] -= sinal * sobraX * parteA;
            x[b] += sinal * sobraX * parteB;
        }
    }

    /** Quadtree: de longe, um grupo de cards age como um ponto só. */
    private static final class Celula {
        private final double cx;
        private final double cy;
        private final double meia;
        private double massaTotal;
        private double mx;
        private double my;
        private int unico = -1;
        private Celula[] filhas;

        Celula(double cx, double cy, double meia) {
            this.cx = cx;
            this.cy = cy;
            this.meia = meia;
        }

        void inserir(int i, double[] x, double[] y, double[] massa, int profundidade) {
            double nova = massaTotal + massa[i];
            mx = (mx * massaTotal + x[i] * massa[i]) / nova;
            my = (my * massaTotal + y[i] * massa[i]) / nova;
            boolean vazia = massaTotal == 0;
            massaTotal = nova;
            if (vazia) {
                unico = i;
                return;
            }
            if (profundidade >= 32) {
                return;
            }
            if (filhas == null) {
                filhas = new Celula[4];
                int antigo = unico;
                unico = -1;
                filha(antigo, x, y).inserir(antigo, x, y, massa, profundidade + 1);
            }
            filha(i, x, y).inserir(i, x, y, massa, profundidade + 1);
        }

        private Celula filha(int i, double[] x, double[] y) {
            int q = (x[i] >= cx ? 1 : 0) + (y[i] >= cy ? 2 : 0);
            if (filhas[q] == null) {
                double m = meia / 2;
                filhas[q] = new Celula(cx + (q % 2 == 1 ? m : -m), cy + (q >= 2 ? m : -m), m);
            }
            return filhas[q];
        }

        void aplicar(int i, double[] x, double[] y, double[] vx, double[] vy, double forca) {
            if (massaTotal == 0 || unico == i) {
                return;
            }
            double dx = x[i] - mx;
            double dy = y[i] - my;
            double d2 = Math.max(100, dx * dx + dy * dy);
            if (filhas == null || (2 * meia) * (2 * meia) / d2 < THETA * THETA) {
                double k = forca * massaTotal / d2;
                vx[i] += dx * k;
                vy[i] += dy * k;
                return;
            }
            for (Celula filha : filhas) {
                if (filha != null) {
                    filha.aplicar(i, x, y, vx, vy, forca);
                }
            }
        }
    }
}

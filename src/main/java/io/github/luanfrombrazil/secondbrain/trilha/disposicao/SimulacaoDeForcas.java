package io.github.luanfrombrazil.secondbrain.trilha.disposicao;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.trilha.Conexao;
import io.github.luanfrombrazil.secondbrain.trilha.Link;
import io.github.luanfrombrazil.secondbrain.trilha.Trilha;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A física do grafo, passo a passo. Todos os cards se repelem, cada conexão ou link é uma mola
 * e uma gravidade leve puxa tudo para o centro. Cada card tem velocidade e atrito, então o
 * movimento é suave.
 * <p>
 * A "energia" (alfa) começa em 1 e vai caindo até a simulação parar. Arrastar um card devolve
 * energia, e os vizinhos reagem.
 */
public final class SimulacaoDeForcas {

    /** A distância "ideal" entre dois cards ligados. */
    private static final double K = 320;
    private static final double GRAVIDADE = 0.05;
    private static final double ATRITO = 0.6;
    private static final double VELOCIDADE_MAX = 60;
    private static final double QUEDA_DO_ALFA = 0.02;
    private static final double ALFA_MINIMO = 0.002;
    private static final double ALFA_AO_ARRASTAR = 0.3;

    private final List<Card> cards;
    private final Map<Card, Integer> indice = new HashMap<>();
    private final List<int[]> molas = new ArrayList<>();
    private final double[] x;
    private final double[] y;
    private final double[] vx;
    private final double[] vy;
    private double alfa = 1;

    public SimulacaoDeForcas(Trilha trilha) {
        cards = List.copyOf(trilha.cards());
        int n = cards.size();
        x = new double[n];
        y = new double[n];
        vx = new double[n];
        vy = new double[n];
        for (int i = 0; i < n; i++) {
            Card card = cards.get(i);
            indice.put(card, i);
            // Desvio mínimo para nenhum card ficar exatamente em cima do outro.
            x[i] = card.xProperty().get() + i * 0.1;
            y[i] = card.yProperty().get() + i * 0.1;
        }
        for (Conexao c : trilha.conexoes()) {
            molas.add(new int[] {indice.get(c.origem()), indice.get(c.destino())});
        }
        for (Link l : trilha.links()) {
            molas.add(new int[] {indice.get(l.a()), indice.get(l.b())});
        }
    }

    public boolean parada() {
        return alfa < ALFA_MINIMO;
    }

    /** Devolve energia à simulação, como quando um card é arrastado. */
    public void aquecer() {
        alfa = Math.max(alfa, ALFA_AO_ARRASTAR);
    }

    /**
     * Avança um passo.
     *
     * @param preso o card sendo arrastado: ele segue o mouse e não sofre forças; pode ser nulo
     */
    public void passo(Card preso) {
        int n = cards.size();
        int fixo = -1;
        if (preso != null && indice.containsKey(preso)) {
            fixo = indice.get(preso);
            x[fixo] = preso.xProperty().get();
            y[fixo] = preso.yProperty().get();
        }

        double[] fx = new double[n];
        double[] fy = new double[n];

        // Repulsão entre todos os pares: K² / d.
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double dx = x[i] - x[j];
                double dy = y[i] - y[j];
                double d = Math.max(Math.hypot(dx, dy), 1);
                double f = K * K / d;
                fx[i] += dx / d * f;
                fy[i] += dy / d * f;
                fx[j] -= dx / d * f;
                fy[j] -= dy / d * f;
            }
        }

        // Atração nas molas: d² / K.
        for (int[] mola : molas) {
            int a = mola[0];
            int b = mola[1];
            double dx = x[a] - x[b];
            double dy = y[a] - y[b];
            double d = Math.max(Math.hypot(dx, dy), 1);
            double f = d * d / K;
            fx[a] -= dx / d * f;
            fy[a] -= dy / d * f;
            fx[b] += dx / d * f;
            fy[b] += dy / d * f;
        }

        // Gravidade em direção ao centro de massa.
        double cx = 0;
        double cy = 0;
        for (int i = 0; i < n; i++) {
            cx += x[i] / n;
            cy += y[i] / n;
        }
        for (int i = 0; i < n; i++) {
            fx[i] -= (x[i] - cx) * GRAVIDADE;
            fy[i] -= (y[i] - cy) * GRAVIDADE;
        }

        // Força vira velocidade (com atrito), velocidade vira posição.
        for (int i = 0; i < n; i++) {
            if (i == fixo) {
                vx[i] = 0;
                vy[i] = 0;
                continue;
            }
            vx[i] = (vx[i] + fx[i] * alfa * 0.1) * ATRITO;
            vy[i] = (vy[i] + fy[i] * alfa * 0.1) * ATRITO;
            double v = Math.hypot(vx[i], vy[i]);
            if (v > VELOCIDADE_MAX) {
                vx[i] *= VELOCIDADE_MAX / v;
                vy[i] *= VELOCIDADE_MAX / v;
            }
            x[i] += vx[i];
            y[i] += vy[i];
        }

        alfa -= alfa * QUEDA_DO_ALFA;
    }

    /** Escreve as posições nos cards (menos no que está sendo arrastado). */
    public void aplicarNosCards(Card preso) {
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            if (card != preso) {
                card.xProperty().set(x[i]);
                card.yProperty().set(y[i]);
            }
        }
    }

    public Map<Card, Point2D> posicoes() {
        Map<Card, Point2D> posicoes = new HashMap<>();
        for (int i = 0; i < cards.size(); i++) {
            posicoes.put(cards.get(i), new Point2D(x[i], y[i]));
        }
        return posicoes;
    }
}

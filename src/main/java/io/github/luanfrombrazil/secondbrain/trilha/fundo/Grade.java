package io.github.luanfrombrazil.secondbrain.trilha.fundo;

import javafx.scene.paint.Color;

import java.util.function.DoubleConsumer;

/**
 * A conta da grade dinâmica, usada pelos fundos pautado, pontilhado e quadriculado.
 * <p>
 * A grade tem dois níveis na tela: o fino e o forte (5 vezes maior). Ao aproximar, quando o
 * fino fica largo demais, ele vira o forte e um fino novo, 5 vezes menor, surge aos poucos.
 * Ao afastar, acontece o contrário. Assim sempre há uma grade de tamanho confortável.
 */
final class Grade {

    /** Tamanho do quadrinho no zoom 100%, em pixels do mundo. */
    static final double BASE = 24;
    static final int DIVISOES = 5;

    static final Color FINA = Color.web("#ffffff", 0.035);
    static final Color FORTE = Color.web("#ffffff", 0.07);

    private Grade() {
    }

    /**
     * O tamanho na tela do nível fino, sempre entre {@code minimo} e {@code minimo} × 5.
     * Abaixo do mínimo, o nível fino some e o forte assume o lugar dele.
     */
    static double passoFino(double escala, double minimo) {
        double passo = BASE * escala;
        while (passo < minimo) passo *= DIVISOES;
        while (passo >= minimo * DIVISOES) passo /= DIVISOES;
        return passo;
    }

    /** O fino aparece aos poucos: transparente quando acabou de surgir, inteiro quando chega a 5×. */
    static double alfaFino(double passo, double minimo) {
        return Math.clamp((passo - minimo) / (minimo * (DIVISOES - 1)), 0, 1);
    }

    /** Chama {@code desenhar} com cada posição da grade (em pixels da tela) entre 0 e {@code limite}. */
    static void percorrer(double origem, double passo, double limite, DoubleConsumer desenhar) {
        double primeira = ((origem % passo) + passo) % passo;
        for (double v = primeira; v <= limite; v += passo) {
            desenhar.accept(v);
        }
    }

    /** Uma linha de 1 px só fica nítida no meio do pixel. */
    static double nitida(double v) {
        return Math.floor(v) + 0.5;
    }

    static Color comAlfa(Color cor, double alfa) {
        return Color.color(cor.getRed(), cor.getGreen(), cor.getBlue(), Math.clamp(cor.getOpacity() * alfa, 0, 1));
    }
}

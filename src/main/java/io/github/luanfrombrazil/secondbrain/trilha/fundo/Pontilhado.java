package io.github.luanfrombrazil.secondbrain.trilha.fundo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Pontos nos cruzamentos da grade. Os pontos precisam de mais espaço que as linhas para não
 * virarem uma textura, então o nível fino só aparece a partir de 16 px. O ponto cresce um pouco
 * com o zoom e é encaixado no pixel, para não tremer enquanto a câmera anda.
 */
public final class Pontilhado implements Fundo {

    private static final double MINIMO = 16;
    private static final Color PONTO = Color.web("#ffffff", 0.12);

    @Override
    public String nome() {
        return "Pontilhado";
    }

    @Override
    public void desenhar(GraphicsContext g, double largura, double altura, double x, double y, double escala) {
        double passo = Grade.passoFino(escala, MINIMO);
        double forte = passo * Grade.DIVISOES;

        // Nível fino, surgindo aos poucos.
        g.setFill(Grade.comAlfa(PONTO, Grade.alfaFino(passo, MINIMO) * 0.6));
        pontos(g, largura, altura, x, y, passo, tamanho(passo));

        // Nível forte, sempre visível.
        g.setFill(PONTO);
        pontos(g, largura, altura, x, y, forte, tamanho(passo) + 1);
    }

    /** De 1 a 2 px conforme o espaço entre os pontos. */
    private static double tamanho(double passo) {
        return passo < MINIMO * 2.5 ? 1 : 2;
    }

    private void pontos(GraphicsContext g, double largura, double altura, double x, double y, double passo, double tamanho) {
        Grade.percorrer(x, passo, largura, px ->
                Grade.percorrer(y, passo, altura, py ->
                        g.fillRect(Math.round(px - tamanho / 2), Math.round(py - tamanho / 2), tamanho, tamanho)));
    }
}

package io.github.luanfrombrazil.secondbrain.trilha.fundo;

import javafx.scene.canvas.GraphicsContext;

/** Linhas horizontais, como um caderno, também com os dois níveis da grade dinâmica. */
public final class Pautado implements Fundo {

    private static final double MINIMO = 8;

    @Override
    public String nome() {
        return "Pautado";
    }

    @Override
    public void desenhar(GraphicsContext g, double largura, double altura, double x, double y, double escala) {
        double passo = Grade.passoFino(escala, MINIMO);
        g.setLineWidth(1);

        g.setStroke(Grade.comAlfa(Grade.FINA, Grade.alfaFino(passo, MINIMO)));
        Grade.percorrer(y, passo, altura, v -> g.strokeLine(0, Grade.nitida(v), largura, Grade.nitida(v)));

        g.setStroke(Grade.FORTE);
        Grade.percorrer(y, passo * Grade.DIVISOES, altura, v -> g.strokeLine(0, Grade.nitida(v), largura, Grade.nitida(v)));
    }
}

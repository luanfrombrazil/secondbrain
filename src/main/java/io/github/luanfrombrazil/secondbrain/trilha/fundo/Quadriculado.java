package io.github.luanfrombrazil.secondbrain.trilha.fundo;

import javafx.scene.canvas.GraphicsContext;

/** A grade dinâmica: quadrinhos menores surgem conforme o zoom aproxima. */
public final class Quadriculado implements Fundo {

    private static final double MINIMO = 8;

    @Override
    public String nome() {
        return "Quadriculado";
    }

    @Override
    public void desenhar(GraphicsContext g, double largura, double altura, double x, double y, double escala) {
        double passo = Grade.passoFino(escala, MINIMO);
        g.setLineWidth(1);

        g.setStroke(Grade.comAlfa(Grade.FINA, Grade.alfaFino(passo, MINIMO)));
        linhas(g, largura, altura, x, y, passo);

        g.setStroke(Grade.FORTE);
        linhas(g, largura, altura, x, y, passo * Grade.DIVISOES);
    }

    private void linhas(GraphicsContext g, double largura, double altura, double x, double y, double passo) {
        Grade.percorrer(x, passo, largura, v -> g.strokeLine(Grade.nitida(v), 0, Grade.nitida(v), altura));
        Grade.percorrer(y, passo, altura, v -> g.strokeLine(0, Grade.nitida(v), largura, Grade.nitida(v)));
    }
}

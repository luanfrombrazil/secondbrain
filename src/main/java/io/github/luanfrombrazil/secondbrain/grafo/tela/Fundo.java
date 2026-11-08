package io.github.luanfrombrazil.secondbrain.grafo.tela;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** O fundo do espaço. A grade tem dois níveis: ao aproximar, quadrinhos 5 vezes menores surgem aos poucos. */
public enum Fundo {
    LISO("Liso"), PAUTADO("Pautado"), PONTILHADO("Pontilhado"), QUADRICULADO("Quadriculado");

    private static final double PASSO_MUNDO = 120;
    private static final double MENOR_PASSO_NA_TELA = 22;
    private static final Color COR = Color.web("#8d93a3");
    private static final double OPACIDADE = 0.16;

    private final String nome;

    Fundo(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return nome;
    }

    void desenhar(GraphicsContext g, Camera c, double largura, double altura) {
        if (this == LISO) {
            return;
        }
        double passoMenor = PASSO_MUNDO * c.escala();
        while (passoMenor < MENOR_PASSO_NA_TELA) {
            passoMenor *= 5;
        }
        while (passoMenor >= MENOR_PASSO_NA_TELA * 5) {
            passoMenor /= 5;
        }
        double t = Math.log(passoMenor / MENOR_PASSO_NA_TELA) / Math.log(5);
        double surgindo = t * t * (3 - 2 * t);
        double passoMaior = passoMenor * 5;

        double origemX = c.telaX(0);
        double origemY = c.telaY(0);
        g.setStroke(COR);
        g.setFill(COR);
        g.setLineWidth(1);
        g.setGlobalAlpha(OPACIDADE * surgindo);
        marcar(g, passoMenor, origemX, origemY, largura, altura);
        g.setGlobalAlpha(OPACIDADE);
        marcar(g, passoMaior, origemX, origemY, largura, altura);
        g.setGlobalAlpha(1);
    }

    private void marcar(GraphicsContext g, double passo, double ox, double oy, double largura, double altura) {
        double x0 = ox - Math.ceil(ox / passo) * passo;
        double y0 = oy - Math.ceil(oy / passo) * passo;
        switch (this) {
            case PAUTADO -> linhasHorizontais(g, passo, y0, largura, altura);
            case QUADRICULADO -> {
                linhasHorizontais(g, passo, y0, largura, altura);
                for (double x = x0; x < largura; x += passo) {
                    double px = Math.floor(x) + 0.5;
                    g.strokeLine(px, 0, px, altura);
                }
            }
            case PONTILHADO -> {
                for (double x = x0; x < largura; x += passo) {
                    for (double y = y0; y < altura; y += passo) {
                        g.fillRect(x - 1, y - 1, 2, 2);
                    }
                }
            }
            case LISO -> {
            }
        }
    }

    private void linhasHorizontais(GraphicsContext g, double passo, double y0, double largura, double altura) {
        for (double y = y0; y < altura; y += passo) {
            double py = Math.floor(y) + 0.5;
            g.strokeLine(0, py, largura, py);
        }
    }
}

package io.github.luanfrombrazil.secondbrain.trilha.fundo;

import javafx.scene.canvas.GraphicsContext;

/** Sem linhas: só a cor de fundo. */
public final class Liso implements Fundo {

    @Override
    public String nome() {
        return "Liso";
    }

    @Override
    public void desenhar(GraphicsContext g, double largura, double altura, double x, double y, double escala) {
    }
}

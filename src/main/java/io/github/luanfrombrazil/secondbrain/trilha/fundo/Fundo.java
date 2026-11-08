package io.github.luanfrombrazil.secondbrain.trilha.fundo;

import javafx.scene.canvas.GraphicsContext;

/** O fundo do espaço de uma trilha. Cada estilo é uma implementação. */
public interface Fundo {

    String nome();

    /**
     * Desenha o fundo na tela.
     *
     * @param x      onde a origem do mundo cai na tela (deslocamento da câmera)
     * @param y      idem, na vertical
     * @param escala o zoom atual
     */
    void desenhar(GraphicsContext g, double largura, double altura, double x, double y, double escala);
}

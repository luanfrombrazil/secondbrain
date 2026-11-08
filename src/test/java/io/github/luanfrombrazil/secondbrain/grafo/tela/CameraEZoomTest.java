package io.github.luanfrombrazil.secondbrain.grafo.tela;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CameraEZoomTest {

    private static final double[] LIMITES = {0.14, 0.30, 0.55, 0.86};

    @Test
    void zoomMantemOPontoEmbaixoDoCursor() {
        Camera c = new Camera();
        c.arrastar(120, -40);
        double mundoX = c.mundoX(300);
        double mundoY = c.mundoY(200);
        c.zoomNoCursor(300, 200, 1.7);
        c.pularParaOAlvo();
        assertEquals(mundoX, c.mundoX(300), 1e-9);
        assertEquals(mundoY, c.mundoY(200), 1e-9);
    }

    @Test
    void cadaEscalaCaiNoNivelCerto() {
        assertEquals(NivelDeZoom.MAIS_AFASTADO, NivelDeZoom.para(0.05, NivelDeZoom.MEDIO, LIMITES));
        assertEquals(NivelDeZoom.MUITO_AFASTADO, NivelDeZoom.para(0.2, NivelDeZoom.MEDIO, LIMITES));
        assertEquals(NivelDeZoom.AFASTADO, NivelDeZoom.para(0.42, NivelDeZoom.MEDIO, LIMITES));
        assertEquals(NivelDeZoom.MEDIO, NivelDeZoom.para(0.72, NivelDeZoom.MEDIO, LIMITES));
        assertEquals(NivelDeZoom.PROXIMO, NivelDeZoom.para(1, NivelDeZoom.MEDIO, LIMITES));
    }

    @Test
    void histereseSeguraONivelPertoDoLimite() {
        assertEquals(NivelDeZoom.MEDIO, NivelDeZoom.para(0.87, NivelDeZoom.MEDIO, LIMITES));
        assertEquals(NivelDeZoom.PROXIMO, NivelDeZoom.para(0.84, NivelDeZoom.PROXIMO, LIMITES));
    }
}

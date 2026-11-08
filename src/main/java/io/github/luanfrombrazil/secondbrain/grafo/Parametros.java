package io.github.luanfrombrazil.secondbrain.grafo;

import java.util.Locale;

/** Os números ajustáveis das disposições e do zoom. Os valores daqui são os padrões. */
public final class Parametros {

    // Por dependência
    public double folgaHorizontal = 130;
    public double folgaVertical = 40;

    // Radial
    public double passoRadial = 400;

    // Por forças
    public double repulsao = 14000;
    public double mola = 0.12;
    public double comprimentoMola = 340;
    public double gravidade = 0.015;
    public double atrito = 0.6;
    public double resfriamento = 0.02;
    public double forcaPasta = 0.03;

    // Card solto
    public double raioMagnetico = 50;

    // Limites de zoom entre os cinco níveis, do mais longe para o mais perto
    public final double[] limitesDeZoom = {0.14, 0.30, 0.55, 0.86};

    public String comoTexto() {
        return String.format(Locale.ROOT, """
                folgaHorizontal = %.0f
                folgaVertical = %.0f
                passoRadial = %.0f
                repulsao = %.0f
                mola = %.3f
                comprimentoMola = %.0f
                gravidade = %.3f
                atrito = %.2f
                resfriamento = %.3f
                forcaPasta = %.3f
                raioMagnetico = %.0f
                limitesDeZoom = %.2f, %.2f, %.2f, %.2f
                """, folgaHorizontal, folgaVertical, passoRadial, repulsao, mola, comprimentoMola,
                gravidade, atrito, resfriamento, forcaPasta, raioMagnetico,
                limitesDeZoom[0], limitesDeZoom[1], limitesDeZoom[2], limitesDeZoom[3]);
    }
}

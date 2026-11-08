package io.github.luanfrombrazil.secondbrain.grafo.tela;

/** Os níveis do zoom semântico, do mais longe para o mais perto. */
public enum NivelDeZoom {
    MAIS_AFASTADO, MUITO_AFASTADO, AFASTADO, MEDIO, PROXIMO;

    /** Margem em volta de cada limite, para o card não piscar quando o zoom para em cima dele. */
    private static final double HISTERESE = 0.05;

    /** @param limites a escala em que cada nível passa para o seguinte */
    public static NivelDeZoom para(double escala, NivelDeZoom atual, double[] limites) {
        int nivel = 0;
        while (nivel < limites.length) {
            double margem = atual.ordinal() > nivel ? 1 - HISTERESE : 1 + HISTERESE;
            if (escala < limites[nivel] * margem) {
                break;
            }
            nivel++;
        }
        return values()[nivel];
    }
}

package io.github.luanfrombrazil.secondbrain.grafo.tela;

/** A câmera da tela infinita: tela = mundo × escala + deslocamento. Anda suave até o alvo. */
public final class Camera {

    public static final double ESCALA_MINIMA = 0.03;
    public static final double ESCALA_MAXIMA = 2.5;
    private static final double VELOCIDADE = 14;
    private static final double PERTO_O_BASTANTE = 0.05;

    private double escala = 1;
    private double dx;
    private double dy;
    private double alvoEscala = 1;
    private double alvoDx;
    private double alvoDy;

    public double escala() {
        return escala;
    }

    public double telaX(double mundoX) {
        return mundoX * escala + dx;
    }

    public double telaY(double mundoY) {
        return mundoY * escala + dy;
    }

    public double mundoX(double telaX) {
        return (telaX - dx) / escala;
    }

    public double mundoY(double telaY) {
        return (telaY - dy) / escala;
    }

    /** O ponto do mundo que está embaixo do cursor continua embaixo dele. */
    public void zoomNoCursor(double cursorX, double cursorY, double fator) {
        double nova = Math.clamp(alvoEscala * fator, ESCALA_MINIMA, ESCALA_MAXIMA);
        double mundoX = (cursorX - alvoDx) / alvoEscala;
        double mundoY = (cursorY - alvoDy) / alvoEscala;
        alvoEscala = nova;
        alvoDx = cursorX - mundoX * nova;
        alvoDy = cursorY - mundoY * nova;
    }

    /** Arrastar é direto: o mundo acompanha o mouse sem atraso. */
    public void arrastar(double deltaX, double deltaY) {
        dx += deltaX;
        dy += deltaY;
        alvoDx += deltaX;
        alvoDy += deltaY;
    }

    public void enquadrar(double minX, double minY, double maxX, double maxY, double largura, double altura) {
        double margem = 60;
        double escalaX = (largura - 2 * margem) / Math.max(1, maxX - minX);
        double escalaY = (altura - 2 * margem) / Math.max(1, maxY - minY);
        alvoEscala = Math.clamp(Math.min(escalaX, escalaY), ESCALA_MINIMA, 1);
        alvoDx = largura / 2 - (minX + maxX) / 2 * alvoEscala;
        alvoDy = altura / 2 - (minY + maxY) / 2 * alvoEscala;
    }

    public void pularParaOAlvo() {
        escala = alvoEscala;
        dx = alvoDx;
        dy = alvoDy;
    }

    /** Devolve verdadeiro enquanto ainda está andando. */
    public boolean atualizar(double segundos) {
        double t = 1 - Math.exp(-segundos * VELOCIDADE);
        if (Math.abs(alvoEscala - escala) * 1000 < PERTO_O_BASTANTE
                && Math.abs(alvoDx - dx) < PERTO_O_BASTANTE
                && Math.abs(alvoDy - dy) < PERTO_O_BASTANTE) {
            boolean mudou = escala != alvoEscala || dx != alvoDx || dy != alvoDy;
            pularParaOAlvo();
            return mudou;
        }
        escala += (alvoEscala - escala) * t;
        dx += (alvoDx - dx) * t;
        dy += (alvoDy - dy) * t;
        return true;
    }
}

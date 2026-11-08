package io.github.luanfrombrazil.secondbrain.grafo.tela;

import io.github.luanfrombrazil.secondbrain.grafo.Aresta;
import io.github.luanfrombrazil.secondbrain.grafo.No;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Desenha o grafo em coordenadas de tela, um desenho de card para cada nível de zoom. */
final class DesenhoDoGrafo {

    static final Color COR_BLOQUEADO = Color.web("#e0524d");
    static final Color COR_DISPONIVEL = Color.web("#e7b53c");
    static final Color COR_EM_ANDAMENTO = Color.web("#4f8fea");
    static final Color COR_CONCLUIDO = Color.web("#47b26b");

    private static final Color COR_ESPACO = Color.web("#17181c");
    private static final Color COR_PASTA = Color.web("#20232b");
    private static final Color COR_CARD = Color.web("#2a2c33");
    private static final Color COR_BORDA = Color.web("#3c3f48");
    private static final Color COR_SELECAO = Color.web("#f0f1f4");
    private static final Color COR_TEXTO = Color.web("#ecedf0");
    private static final Color COR_TEXTO_FRACO = Color.web("#9ea3ad");
    private static final Color COR_CONEXAO = Color.web("#7d828e");
    private static final Color COR_DESTAQUE = Color.web("#d7dae1");
    private static final Color COR_LINK = Color.web("#8f7fd1");
    private static final Color COR_TRILHO = Color.web("#454852");
    private static final Color COR_PROGRESSO = Color.web("#8bb3f0");
    private static final Color COR_SOMBRA = Color.web("#ffffff", 0.05);
    private static final double LARGURA_DA_NOTA = 150;
    private static final double ALTURA_DA_NOTA = 40;

    private final Map<Integer, Font> fontes = new HashMap<>();
    private final Map<Integer, Font> fontesFortes = new HashMap<>();

    int desenhar(TelaDoGrafo t, GraphicsContext g, Camera c, double largura, double altura) {
        g.setGlobalAlpha(1);
        g.setFill(COR_ESPACO);
        g.fillRect(0, 0, largura, altura);
        if (t.agruparPorPasta) {
            desenharPastas(t, g, c);
        }
        t.fundo.desenhar(g, c, largura, altura);

        if (t.mostrarConexoes) {
            desenharConexoes(t, g, c, largura, altura);
        }
        if (t.nivel == NivelDeZoom.PROXIMO) {
            if (t.mostrarLinks) {
                desenharLinks(t, g, c);
            }
            desenharPontuais(t, g, c, largura, altura);
        }

        int desenhados = 0;
        for (Cartao cartao : t.cartoes.values()) {
            TelaDoGrafo.Visual v = t.visuais.get(cartao.id());
            if (v == null || foraDaTela(c, v, largura, altura)) {
                continue;
            }
            desenhados++;
            if (t.trocaDeNivel < 1) {
                g.setGlobalAlpha(1 - t.trocaDeNivel);
                desenharCard(t, g, c, cartao, v, t.nivelAnterior);
                g.setGlobalAlpha(t.trocaDeNivel);
            }
            desenharCard(t, g, c, cartao, v, t.nivel);
            g.setGlobalAlpha(1);
        }
        desenharDica(t, g, c);
        return desenhados;
    }

    /** O card sob o ponto da tela, ou nulo. */
    String cardEm(TelaDoGrafo t, Camera c, double telaX, double telaY) {
        double mundoX = c.mundoX(telaX);
        double mundoY = c.mundoY(telaY);
        String achado = null;
        for (Cartao cartao : t.cartoes.values()) {
            TelaDoGrafo.Visual v = t.visuais.get(cartao.id());
            if (v == null) {
                continue;
            }
            if (t.nivel == NivelDeZoom.MAIS_AFASTADO) {
                double raio = raioDaBolinha(cartao) + 3;
                if (Math.hypot(c.telaX(v.x) - telaX, c.telaY(v.y) - telaY) <= raio) {
                    achado = cartao.id();
                }
            } else if (Math.abs(mundoX - v.x) <= No.LARGURA / 2 && Math.abs(mundoY - v.y) <= No.ALTURA / 2) {
                achado = cartao.id();
            }
        }
        return achado;
    }

    private boolean foraDaTela(Camera c, TelaDoGrafo.Visual v, double largura, double altura) {
        double folga = Math.max(20, No.LARGURA * c.escala());
        double x = c.telaX(v.x);
        double y = c.telaY(v.y);
        return x < -folga || y < -folga || x > largura + folga || y > altura + folga;
    }

    // Pastas: envoltória convexa, de cantos arredondados, cor sólida atrás da grade.

    private void desenharPastas(TelaDoGrafo t, GraphicsContext g, Camera c) {
        double arredondamento = 70 * c.escala();
        List<Runnable> nomes = new ArrayList<>();
        for (var pasta : t.pastas().entrySet()) {
            List<double[]> pontos = new ArrayList<>();
            for (String id : pasta.getValue()) {
                TelaDoGrafo.Visual v = t.visuais.get(id);
                if (v == null) {
                    continue;
                }
                for (int sx = -1; sx <= 1; sx += 2) {
                    for (int sy = -1; sy <= 1; sy += 2) {
                        pontos.add(new double[]{
                                c.telaX(v.x + sx * No.LARGURA / 2),
                                c.telaY(v.y + sy * No.ALTURA / 2)});
                    }
                }
            }
            if (pontos.size() < 4) {
                continue;
            }
            List<double[]> casco = envoltoria(pontos);
            double[] xs = casco.stream().mapToDouble(p -> p[0]).toArray();
            double[] ys = casco.stream().mapToDouble(p -> p[1]).toArray();
            g.setFill(COR_PASTA);
            g.setStroke(COR_PASTA);
            g.setLineWidth(arredondamento);
            g.setLineJoin(StrokeLineJoin.ROUND);
            g.fillPolygon(xs, ys, xs.length);
            g.strokePolygon(xs, ys, xs.length);
            if (t.nivel.compareTo(NivelDeZoom.MUITO_AFASTADO) >= 0) {
                double topo = casco.stream().mapToDouble(p -> p[1]).min().orElse(0);
                double esquerda = casco.stream().filter(p -> p[1] < topo + 2)
                        .mapToDouble(p -> p[0]).min().orElse(0);
                String nome = pasta.getKey();
                nomes.add(() -> g.fillText(nome, esquerda, topo - arredondamento / 2 - 2));
            }
        }
        g.setLineJoin(StrokeLineJoin.MITER);
        // Depois de todas as áreas, para uma pasta não cobrir o nome da outra.
        g.setFill(COR_TEXTO_FRACO);
        g.setFont(fonte(Math.clamp(26 * c.escala(), 11, 22), false));
        g.setTextBaseline(VPos.BOTTOM);
        nomes.forEach(Runnable::run);
    }

    /** Cadeia monótona de Andrew. */
    private static List<double[]> envoltoria(List<double[]> pontos) {
        List<double[]> ordenados = new ArrayList<>(pontos);
        ordenados.sort(Comparator.<double[]>comparingDouble(p -> p[0]).thenComparingDouble(p -> p[1]));
        List<double[]> casco = new ArrayList<>();
        for (int volta = 0; volta < 2; volta++) {
            int inicio = casco.size();
            for (double[] p : ordenados) {
                while (casco.size() >= inicio + 2
                        && cruz(casco.get(casco.size() - 2), casco.getLast(), p) <= 0) {
                    casco.removeLast();
                }
                casco.add(p);
            }
            casco.removeLast();
            java.util.Collections.reverse(ordenados);
        }
        return casco;
    }

    private static double cruz(double[] o, double[] a, double[] b) {
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]);
    }

    // Setas: pai com dois filhos ou mais, todas curvas; pai com um filho, reta.

    private void desenharConexoes(TelaDoGrafo t, GraphicsContext g, Camera c, double largura, double altura) {
        boolean bolinhas = t.nivel == NivelDeZoom.MAIS_AFASTADO;
        String foco = t.sobMouse != null ? t.sobMouse : t.selecionado;
        double espessura = Math.clamp(2.2 * c.escala(), 1, 2.6);
        for (Aresta a : t.conexoes()) {
            boolean destaque = foco != null && (a.origem().equals(foco) || a.destino().equals(foco));
            if (bolinhas && !destaque) {
                continue;
            }
            TelaDoGrafo.Visual o = t.visuais.get(a.origem());
            TelaDoGrafo.Visual d = t.visuais.get(a.destino());
            if (o == null || d == null || segmentoFora(c, o, d, largura, altura)) {
                continue;
            }
            Color cor = destaque ? COR_DESTAQUE : COR_CONEXAO;
            g.setStroke(cor);
            g.setFill(cor);
            g.setLineWidth(destaque ? espessura + 0.8 : espessura);
            desenharSeta(t, g, c, a, o, d, bolinhas);
        }
    }

    private boolean segmentoFora(Camera c, TelaDoGrafo.Visual o, TelaDoGrafo.Visual d, double largura, double altura) {
        double folga = No.LARGURA * c.escala();
        double ax = c.telaX(o.x);
        double bx = c.telaX(d.x);
        double ay = c.telaY(o.y);
        double by = c.telaY(d.y);
        return Math.max(ax, bx) < -folga || Math.min(ax, bx) > largura + folga
                || Math.max(ay, by) < -folga || Math.min(ay, by) > altura + folga;
    }

    private void desenharSeta(TelaDoGrafo t, GraphicsContext g, Camera c, Aresta a,
                              TelaDoGrafo.Visual o, TelaDoGrafo.Visual d, boolean centros) {
        List<String> irmaos = t.grafo.dependentesDe(a.origem());
        boolean curva = irmaos.size() >= 2;
        double sx;
        double sy;
        double ex;
        double ey;
        boolean horizontal = t.tipo == TipoDeDisposicao.DEPENDENCIA && d.x > o.x;
        if (centros) {
            sx = o.x;
            sy = o.y;
            ex = d.x;
            ey = d.y;
        } else if (horizontal) {
            sx = o.x + No.LARGURA / 2;
            sy = o.y;
            ex = d.x - No.LARGURA / 2;
            ey = d.y;
        } else {
            double[] saida = naBorda(o.x, o.y, d.x, d.y);
            double[] chegada = naBorda(d.x, d.y, o.x, o.y);
            sx = saida[0];
            sy = saida[1];
            ex = chegada[0];
            ey = chegada[1];
        }

        double x1 = c.telaX(sx);
        double y1 = c.telaY(sy);
        double x2 = c.telaX(ex);
        double y2 = c.telaY(ey);
        double c2x = x1;
        double c2y = y1;
        g.beginPath();
        g.moveTo(x1, y1);
        if (!curva) {
            g.lineTo(x2, y2);
        } else if (horizontal) {
            double folga = Math.max(30 * c.escala(), (x2 - x1) / 2);
            c2x = x2 - folga;
            c2y = y2;
            g.bezierCurveTo(x1 + folga, y1, c2x, c2y, x2, y2);
        } else {
            double comprimento = Math.max(1, Math.hypot(x2 - x1, y2 - y1));
            double nx = -(y2 - y1) / comprimento;
            double ny = (x2 - x1) / comprimento;
            int indice = irmaos.indexOf(a.destino());
            double arco = comprimento * 0.16 * (indice * 2 < irmaos.size() ? 1 : -1);
            c2x = x1 + (x2 - x1) * 0.67 + nx * arco;
            c2y = y1 + (y2 - y1) * 0.67 + ny * arco;
            g.bezierCurveTo(x1 + (x2 - x1) * 0.33 + nx * arco, y1 + (y2 - y1) * 0.33 + ny * arco,
                    c2x, c2y, x2, y2);
        }
        g.stroke();
        if (!centros) {
            desenharPonta(g, curva ? c2x : x1, curva ? c2y : y1, x2, y2, Math.clamp(11 * c.escala(), 5, 13));
        }
    }

    private static void desenharPonta(GraphicsContext g, double deX, double deY, double x, double y, double tamanho) {
        double angulo = Math.atan2(y - deY, x - deX);
        double abertura = 0.45;
        g.fillPolygon(
                new double[]{x, x - tamanho * Math.cos(angulo - abertura), x - tamanho * Math.cos(angulo + abertura)},
                new double[]{y, y - tamanho * Math.sin(angulo - abertura), y - tamanho * Math.sin(angulo + abertura)},
                3);
    }

    /** Onde a linha que sai do centro de (ax, ay) rumo a (bx, by) cruza a borda do card. */
    private static double[] naBorda(double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        if (dx == 0 && dy == 0) {
            return new double[]{ax, ay};
        }
        double t = Math.min(
                dx == 0 ? Double.MAX_VALUE : No.LARGURA / 2 / Math.abs(dx),
                dy == 0 ? Double.MAX_VALUE : No.ALTURA / 2 / Math.abs(dy));
        return new double[]{ax + dx * t, ay + dy * t};
    }

    private void desenharLinks(TelaDoGrafo t, GraphicsContext g, Camera c) {
        g.setStroke(COR_LINK);
        g.setLineWidth(1.4);
        g.setLineDashes(6, 6);
        for (Aresta a : t.grafo.links()) {
            TelaDoGrafo.Visual o = t.visuais.get(a.origem());
            TelaDoGrafo.Visual d = t.visuais.get(a.destino());
            if (o != null && d != null) {
                g.strokeLine(c.telaX(o.x), c.telaY(o.y), c.telaX(d.x), c.telaY(d.y));
            }
        }
        g.setLineDashes((double[]) null);
    }

    /** As notas pontuais do card em foco: o selecionado, ou o mais perto do centro da tela. */
    private void desenharPontuais(TelaDoGrafo t, GraphicsContext g, Camera c, double largura, double altura) {
        String foco = t.selecionado;
        if (foco == null) {
            double melhor = Double.MAX_VALUE;
            for (var e : t.visuais.entrySet()) {
                double d = Math.hypot(c.telaX(e.getValue().x) - largura / 2, c.telaY(e.getValue().y) - altura / 2);
                if (d < melhor) {
                    melhor = d;
                    foco = e.getKey();
                }
            }
        }
        TelaDoGrafo.Visual v = foco == null ? null : t.visuais.get(foco);
        if (v == null) {
            return;
        }
        List<NotaPontual> notas = new ArrayList<>();
        for (NotaPontual nota : t.pontuais) {
            if (nota.ligadaA().contains(foco)) {
                notas.add(nota);
            }
        }
        double passo = LARGURA_DA_NOTA + 14;
        double inicio = v.x - passo * (notas.size() - 1) / 2;
        double y = v.y - No.ALTURA / 2 - 62;
        for (int i = 0; i < notas.size(); i++) {
            double x = inicio + i * passo;
            g.setStroke(COR_LINK);
            g.setLineWidth(1.2);
            g.setLineDashes(5, 5);
            g.strokeLine(c.telaX(x), c.telaY(y + ALTURA_DA_NOTA / 2), c.telaX(v.x), c.telaY(v.y - No.ALTURA / 2));
            g.setLineDashes((double[]) null);
            double px = c.telaX(x - LARGURA_DA_NOTA / 2);
            double py = c.telaY(y - ALTURA_DA_NOTA / 2);
            double w = LARGURA_DA_NOTA * c.escala();
            double h = ALTURA_DA_NOTA * c.escala();
            g.setFill(COR_CARD);
            g.fillRoundRect(px, py, w, h, 8, 8);
            g.setStroke(COR_LINK);
            g.strokeRoundRect(px, py, w, h, 8, 8);
            texto(g, notas.get(i).titulo(), px + 8 * c.escala(), py + h / 2, w - 16 * c.escala(),
                    12 * c.escala(), false, COR_TEXTO, VPos.CENTER);
        }
    }

    // Cards

    private double raioDaBolinha(Cartao cartao) {
        return 3 + 9 * cartao.peso();
    }

    private void desenharCard(TelaDoGrafo t, GraphicsContext g, Camera c, Cartao cartao,
                              TelaDoGrafo.Visual v, NivelDeZoom nivelDoDesenho) {
        double cx = c.telaX(v.x);
        double cy = c.telaY(v.y);
        boolean selecionado = cartao.id().equals(t.selecionado);
        if (nivelDoDesenho == NivelDeZoom.MAIS_AFASTADO) {
            double r = raioDaBolinha(cartao);
            g.setFill(cartao.corDoEstado());
            g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
            if (selecionado) {
                g.setStroke(COR_SELECAO);
                g.setLineWidth(1.5);
                g.strokeOval(cx - r - 2, cy - r - 2, 2 * r + 4, 2 * r + 4);
            }
            return;
        }

        double e = c.escala();
        double w = No.LARGURA * e;
        double h = No.ALTURA * e;
        double x = cx - w / 2;
        double y = cy - h / 2;
        double canto = Math.max(3, 16 * e);
        g.setFill(COR_SOMBRA);
        g.fillRoundRect(x - 2, y - 2, w + 4, h + 4, canto + 3, canto + 3);
        g.setFill(COR_CARD);
        g.fillRoundRect(x, y, w, h, canto, canto);

        // A faixa do estado acompanha a curva dos cantos de cima.
        double faixa = Math.max(2, 8 * e);
        g.save();
        g.beginPath();
        g.rect(x, y, w, faixa);
        g.clip();
        g.setFill(cartao.corDoEstado());
        g.fillRoundRect(x, y, w, h, canto, canto);
        g.restore();

        g.setStroke(selecionado ? COR_SELECAO : COR_BORDA);
        g.setLineWidth(selecionado ? 2 : 1);
        g.strokeRoundRect(x, y, w, h, canto, canto);

        double margem = 14 * e;
        double larguraUtil = w - 2 * margem;
        double barraY = y + h - 14 * e;
        desenharBarra(g, x + margem, barraY, larguraUtil, Math.max(2, 5 * e), cartao.progresso());

        switch (nivelDoDesenho) {
            case AFASTADO -> {
                g.setFill(COR_TEXTO_FRACO);
                g.fillRect(x + margem, y + 26 * e, larguraUtil * 0.75, 9 * e);
                g.fillRect(x + margem, y + 44 * e, larguraUtil * 0.5, 7 * e);
            }
            case MEDIO -> texto(g, cartao.titulo(), x + margem, y + 42 * e, larguraUtil,
                    21 * e, true, COR_TEXTO, VPos.CENTER);
            case PROXIMO -> desenharCardCompleto(g, cartao, x, y, w, margem, e);
            default -> {
            }
        }
    }

    private void desenharCardCompleto(GraphicsContext g, Cartao cartao, double x, double y, double w, double margem, double e) {
        texto(g, cartao.titulo(), x + margem, y + 28 * e, w - 2 * margem - 22 * e, 15 * e, true, COR_TEXTO, VPos.CENTER);
        if (cartao.temGrafoInterno()) {
            g.setStroke(COR_TEXTO_FRACO);
            g.setLineWidth(1.2);
            g.strokeRect(x + w - margem - 12 * e, y + 22 * e, 12 * e, 12 * e);
        }
        double linha = y + 50 * e;
        int feitas = cartao.tarefasFeitas();
        List<String> tarefas = cartao.tarefas();
        for (int i = 0; i < Math.min(2, tarefas.size()); i++) {
            double lado = 9 * e;
            g.setStroke(COR_TEXTO_FRACO);
            g.setLineWidth(1);
            g.strokeRect(x + margem, linha + i * 17 * e - lado / 2, lado, lado);
            if (i < feitas) {
                g.setFill(COR_PROGRESSO);
                g.fillRect(x + margem + 2 * e, linha + i * 17 * e - lado / 2 + 2 * e, lado - 4 * e, lado - 4 * e);
            }
            texto(g, tarefas.get(i), x + margem + 15 * e, linha + i * 17 * e, w - 2 * margem - 15 * e,
                    11.5 * e, false, COR_TEXTO_FRACO, VPos.CENTER);
        }
        if (!tarefas.isEmpty()) {
            texto(g, feitas + "/" + tarefas.size(), x + w - margem - 28 * e, y + 82 * e, 30 * e,
                    10.5 * e, false, COR_TEXTO_FRACO, VPos.CENTER);
        }
    }

    private void desenharBarra(GraphicsContext g, double x, double y, double largura, double altura, double valor) {
        g.setFill(COR_TRILHO);
        g.fillRoundRect(x, y, largura, altura, altura, altura);
        if (valor > 0) {
            g.setFill(COR_PROGRESSO);
            g.fillRoundRect(x, y, largura * Math.min(1, valor), altura, altura, altura);
        }
    }

    private void desenharDica(TelaDoGrafo t, GraphicsContext g, Camera c) {
        if (t.sobMouse == null || t.nivel.compareTo(NivelDeZoom.AFASTADO) > 0) {
            return;
        }
        Cartao cartao = t.cartoes.get(t.sobMouse);
        TelaDoGrafo.Visual v = t.visuais.get(t.sobMouse);
        if (cartao == null || v == null) {
            return;
        }
        String frase = cartao.titulo() + "  ·  " + Math.round(cartao.progresso() * 100) + "%";
        double largura = frase.length() * 6.9 + 18;
        double x = c.telaX(v.x) - largura / 2;
        double recuo = t.nivel == NivelDeZoom.MAIS_AFASTADO ? 12 : No.ALTURA * c.escala() / 2;
        double y = c.telaY(v.y) - recuo - 34;
        g.setFill(Color.web("#0f1013"));
        g.fillRoundRect(x, y, largura, 26, 8, 8);
        g.setStroke(COR_BORDA);
        g.setLineWidth(1);
        g.strokeRoundRect(x, y, largura, 26, 8, 8);
        texto(g, frase, x + 9, y + 13, largura, 12, false, COR_TEXTO, VPos.CENTER);
    }

    private void texto(GraphicsContext g, String frase, double x, double y, double largura, double tamanho,
                       boolean forte, Color cor, VPos base) {
        int cabem = (int) (largura / (tamanho * (forte ? 0.58 : 0.54)));
        String saida = frase.length() <= cabem ? frase : frase.substring(0, Math.max(0, cabem - 1)) + "…";
        g.setFill(cor);
        g.setFont(fonte(tamanho, forte));
        g.setTextBaseline(base);
        g.fillText(saida, x, y);
    }

    private Font fonte(double tamanho, boolean forte) {
        int chave = (int) Math.round(tamanho * 2);
        return (forte ? fontesFortes : fontes).computeIfAbsent(chave, k -> Font.font(
                "Segoe UI", forte ? FontWeight.SEMI_BOLD : FontWeight.NORMAL, k / 2.0));
    }
}

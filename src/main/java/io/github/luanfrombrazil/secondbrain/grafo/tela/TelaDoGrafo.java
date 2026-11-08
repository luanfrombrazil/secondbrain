package io.github.luanfrombrazil.secondbrain.grafo.tela;

import io.github.luanfrombrazil.secondbrain.grafo.Aresta;
import io.github.luanfrombrazil.secondbrain.grafo.Grafo;
import io.github.luanfrombrazil.secondbrain.grafo.No;
import io.github.luanfrombrazil.secondbrain.grafo.Parametros;
import io.github.luanfrombrazil.secondbrain.grafo.Ponto;
import io.github.luanfrombrazil.secondbrain.grafo.SimulacaoDeForcas;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A tela infinita. Controla câmera, animação e mouse; quem desenha é o {@link DesenhoDoGrafo}.
 * Tudo vai num Canvas, em coordenadas de tela, para o texto sair nítido em qualquer zoom.
 */
public final class TelaDoGrafo extends Region {

    private static final double VELOCIDADE_DOS_CARDS = 7;
    private static final double DURACAO_DA_TROCA_DE_NIVEL = 0.18;
    private static final double ARRASTE_MINIMO = 4;

    /** Onde o card está e para onde vai. */
    static final class Visual {
        double x;
        double y;
        double alvoX;
        double alvoY;

        Visual(Ponto p) {
            x = alvoX = p.x();
            y = alvoY = p.y();
        }
    }

    private final Canvas canvas = new Canvas();
    private final Camera camera = new Camera();
    private final Parametros parametros;
    private final DesenhoDoGrafo desenho = new DesenhoDoGrafo();

    Grafo grafo = new Grafo(List.of(), List.of(), List.of());
    final Map<String, Cartao> cartoes = new LinkedHashMap<>();
    final Map<String, Visual> visuais = new HashMap<>();
    List<NotaPontual> pontuais = List.of();

    TipoDeDisposicao tipo = TipoDeDisposicao.DEPENDENCIA;
    Fundo fundo = Fundo.QUADRICULADO;
    boolean mostrarConexoes = true;
    boolean mostrarLinks = true;
    boolean agruparPorPasta = true;
    NivelDeZoom nivel = NivelDeZoom.MEDIO;
    NivelDeZoom nivelAnterior = NivelDeZoom.MEDIO;
    double trocaDeNivel = 1;
    String selecionado;
    String sobMouse;
    double mouseX;
    double mouseY;

    private SimulacaoDeForcas simulacao;
    private boolean sujo = true;
    private long quadroAnterior;
    private double pressX;
    private double pressY;
    private double ultimoX;
    private double ultimoY;
    private boolean arrastou;
    private int contagemDeSoltos;
    private double fps;
    private int desenhados;

    private final AnimationTimer laco = new AnimationTimer() {
        @Override
        public void handle(long agora) {
            double segundos = quadroAnterior == 0 ? 0.016 : Math.min(0.05, (agora - quadroAnterior) / 1e9);
            quadroAnterior = agora;
            avancar(segundos);
            if (sujo) {
                desenharAgora();
                fps += (1 / Math.max(0.001, segundos) - fps) * 0.1;
            }
        }
    };

    public TelaDoGrafo(Parametros parametros) {
        this.parametros = parametros;
        getChildren().add(canvas);
        setMinSize(0, 0);
        canvas.addEventHandler(MouseEvent.ANY, this::aoMouse);
        canvas.setOnScroll(e -> {
            camera.zoomNoCursor(e.getX(), e.getY(), Math.pow(1.0018, e.getDeltaY()));
            sujo = true;
        });
        laco.start();
    }

    @Override
    protected void layoutChildren() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        sujo = true;
    }

    public void mostrar(Grafo novoGrafo, Map<String, Cartao> novosCartoes, List<NotaPontual> notas) {
        grafo = novoGrafo;
        cartoes.clear();
        cartoes.putAll(novosCartoes);
        pontuais = notas;
        visuais.clear();
        selecionado = null;
        sobMouse = null;
        simulacao = null;
        Map<String, Ponto> posicoes = tipo.disposicao().calcular(grafo, parametros);
        posicoes.forEach((id, p) -> visuais.put(id, new Visual(p)));
        enquadrarAlvos();
        camera.pularParaOAlvo();
        sujo = true;
    }

    /** Trocar ou forçar a disposição leva os soltos para a faixa (D-58) e centraliza a câmera. */
    public void reorganizar(TipoDeDisposicao novo) {
        tipo = novo;
        grafo = grafo.semFixos();
        recalcular();
        enquadrarAlvos();
    }

    /** Um parâmetro mudou: arruma de novo, sem mexer na câmera nem nos soltos. */
    public void recalcular() {
        if (tipo == TipoDeDisposicao.FORCAS) {
            simulacao = new SimulacaoDeForcas(grafo, parametros, posicoesAtuais());
        } else {
            simulacao = null;
            definirAlvos(tipo.disposicao().calcular(grafo, parametros));
        }
        sujo = true;
    }

    /** Cria um card solto num ponto do espaço: fica ali, e os vizinhos se afastam. */
    public void criarSolto(double mundoX, double mundoY) {
        contagemDeSoltos++;
        String id = "solto-" + contagemDeSoltos;
        Ponto ponto = new Ponto(mundoX, mundoY);
        grafo = grafo.comNo(new No(id, null, ponto));
        cartoes.put(id, new Cartao(id, "Card solto " + contagemDeSoltos, DesenhoDoGrafo.COR_DISPONIVEL,
                0, 0.4, List.of(), 0, false));
        visuais.put(id, new Visual(ponto));
        selecionado = id;
        recalcular();
    }

    public void centralizar() {
        enquadrarAlvos();
        sujo = true;
    }

    public void setFundo(Fundo novo) {
        fundo = novo;
        sujo = true;
    }

    public void setMostrarConexoes(boolean valor) {
        mostrarConexoes = valor;
        sujo = true;
    }

    public void setMostrarLinks(boolean valor) {
        mostrarLinks = valor;
        sujo = true;
    }

    public void setAgruparPorPasta(boolean valor) {
        agruparPorPasta = valor;
        sujo = true;
    }

    public void marcarSujo() {
        sujo = true;
    }

    public Camera camera() {
        return camera;
    }

    public NivelDeZoom nivel() {
        return nivel;
    }

    public double fps() {
        return fps;
    }

    public int desenhados() {
        return desenhados;
    }

    public int totalDeCards() {
        return cartoes.size();
    }

    public boolean simulando() {
        return simulacao != null;
    }

    /** Termina animações e simulação na hora. Usado para capturar a tela. */
    public void assentar() {
        if (simulacao != null) {
            while (simulacao.passo()) {
                // até assentar
            }
            aplicar(simulacao.posicoes());
            simulacao = null;
        }
        visuais.values().forEach(v -> {
            v.x = v.alvoX;
            v.y = v.alvoY;
        });
        camera.pularParaOAlvo();
        atualizarNivel();
        trocaDeNivel = 1;
    }

    public void desenharAgora() {
        desenhados = desenho.desenhar(this, canvas.getGraphicsContext2D(), camera, canvas.getWidth(), canvas.getHeight());
        sujo = false;
    }

    public void pararAnimacao() {
        laco.stop();
    }

    public Canvas canvas() {
        return canvas;
    }

    public void enquadrarSemAnimar() {
        enquadrarAlvos();
        camera.pularParaOAlvo();
    }

    private void avancar(double segundos) {
        if (camera.atualizar(segundos)) {
            sujo = true;
        }
        if (simulacao != null) {
            boolean continua = simulacao.passo();
            aplicar(simulacao.posicoes());
            visuais.values().forEach(v -> {
                v.x = v.alvoX;
                v.y = v.alvoY;
            });
            sujo = true;
            if (!continua) {
                simulacao = null;
            }
        } else {
            double t = 1 - Math.exp(-segundos * VELOCIDADE_DOS_CARDS);
            for (Visual v : visuais.values()) {
                double dx = v.alvoX - v.x;
                double dy = v.alvoY - v.y;
                if (dx != 0 || dy != 0) {
                    boolean chegou = Math.abs(dx) < 0.1 && Math.abs(dy) < 0.1;
                    v.x = chegou ? v.alvoX : v.x + dx * t;
                    v.y = chegou ? v.alvoY : v.y + dy * t;
                    sujo = true;
                }
            }
        }
        atualizarNivel();
        if (trocaDeNivel < 1) {
            trocaDeNivel = Math.min(1, trocaDeNivel + segundos / DURACAO_DA_TROCA_DE_NIVEL);
            sujo = true;
        }
    }

    private void atualizarNivel() {
        NivelDeZoom novo = NivelDeZoom.para(camera.escala(), nivel, parametros.limitesDeZoom);
        if (novo != nivel) {
            nivelAnterior = nivel;
            nivel = novo;
            trocaDeNivel = 0;
            sujo = true;
        }
    }

    private Map<String, Ponto> posicoesAtuais() {
        Map<String, Ponto> atuais = new HashMap<>();
        visuais.forEach((id, v) -> atuais.put(id, new Ponto(v.x, v.y)));
        return atuais;
    }

    private void aplicar(Map<String, Ponto> posicoes) {
        definirAlvos(posicoes);
    }

    private void definirAlvos(Map<String, Ponto> posicoes) {
        posicoes.forEach((id, p) -> {
            Visual v = visuais.computeIfAbsent(id, k -> new Visual(p));
            v.alvoX = p.x();
            v.alvoY = p.y();
        });
    }

    private void enquadrarAlvos() {
        if (visuais.isEmpty()) {
            return;
        }
        Map<String, Ponto> alvos = new HashMap<>();
        if (tipo == TipoDeDisposicao.FORCAS && simulacao != null) {
            alvos.putAll(tipo.disposicao().calcular(grafo, parametros));
        } else {
            visuais.forEach((id, v) -> alvos.put(id, new Ponto(v.alvoX, v.alvoY)));
        }
        double minX = alvos.values().stream().mapToDouble(Ponto::x).min().orElse(0) - No.LARGURA / 2;
        double maxX = alvos.values().stream().mapToDouble(Ponto::x).max().orElse(0) + No.LARGURA / 2;
        double minY = alvos.values().stream().mapToDouble(Ponto::y).min().orElse(0) - No.ALTURA / 2;
        double maxY = alvos.values().stream().mapToDouble(Ponto::y).max().orElse(0) + No.ALTURA / 2;
        double largura = getWidth() > 0 ? getWidth() : 1200;
        double altura = getHeight() > 0 ? getHeight() : 800;
        camera.enquadrar(minX, minY, maxX, maxY, largura, altura);
    }

    private void aoMouse(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
            pressX = ultimoX = e.getX();
            pressY = ultimoY = e.getY();
            arrastou = false;
        } else if (e.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            if (Math.hypot(e.getX() - pressX, e.getY() - pressY) > ARRASTE_MINIMO) {
                arrastou = true;
            }
            if (arrastou) {
                camera.arrastar(e.getX() - ultimoX, e.getY() - ultimoY);
                sujo = true;
            }
            ultimoX = e.getX();
            ultimoY = e.getY();
        } else if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
            String agora = desenho.cardEm(this, camera, e.getX(), e.getY());
            if (!java.util.Objects.equals(agora, sobMouse)
                    || (agora != null && nivel.compareTo(NivelDeZoom.MEDIO) < 0)) {
                sobMouse = agora;
                sujo = true;
            }
        } else if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
            sobMouse = null;
            sujo = true;
        } else if (e.getEventType() == MouseEvent.MOUSE_CLICKED
                && e.getButton() == MouseButton.PRIMARY && !arrastou) {
            String clicado = desenho.cardEm(this, camera, e.getX(), e.getY());
            if (e.getClickCount() == 2 && clicado == null) {
                criarSolto(camera.mundoX(e.getX()), camera.mundoY(e.getY()));
            } else if (e.getClickCount() == 1) {
                selecionado = clicado;
            }
            sujo = true;
        }
    }

    /** Os ids que cada pasta agrupa. */
    Map<String, List<String>> pastas() {
        Map<String, List<String>> saida = new LinkedHashMap<>();
        for (No no : grafo.nos()) {
            if (no.pasta() != null) {
                saida.computeIfAbsent(no.pasta(), k -> new ArrayList<>()).add(no.id());
            }
        }
        return saida;
    }

    List<Aresta> conexoes() {
        return grafo.conexoes();
    }
}

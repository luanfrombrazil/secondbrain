package io.github.luanfrombrazil.secondbrain.trilha;

import io.github.luanfrombrazil.secondbrain.card.Card;
import io.github.luanfrombrazil.secondbrain.card.CardView;
import io.github.luanfrombrazil.secondbrain.trilha.disposicao.Disposicao;
import io.github.luanfrombrazil.secondbrain.trilha.disposicao.SimulacaoDeForcas;
import javafx.animation.AnimationTimer;
import io.github.luanfrombrazil.secondbrain.trilha.fundo.Fundo;
import io.github.luanfrombrazil.secondbrain.trilha.fundo.Quadriculado;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

/** O espaço infinito de uma trilha: fundo, cards, conexões, arrastar e zoom. */
public class TrilhaView extends Pane {

    private static final double ZOOM_MIN = 0.2;
    private static final double ZOOM_MAX = 3;
    /** A partir deste zoom os links aparecem. */
    private static final double ZOOM_LINKS = 0.9;
    private static final Duration ANIMACAO = Duration.millis(400);

    private final Trilha trilha;
    private final Canvas tela = new Canvas();
    /** O fundo começa quadriculado; o menu de fundo troca. */
    private final ObjectProperty<Fundo> fundo = new SimpleObjectProperty<>(new Quadriculado());
    private final Group mundo = new Group();
    private final Translate deslocamento = new Translate();
    private final Scale escala = new Scale(1, 1, 0, 0);
    private final Map<Card, CardView> views = new HashMap<>();

    private double arrasteX;
    private double arrasteY;
    /** O último card clicado: o centro da disposição radial. */
    private Card foco;
    /** O card sendo arrastado agora, ou nulo. */
    private Card arrastado;
    /** A física, quando a disposição por forças está ativa; nula nas outras. */
    private SimulacaoDeForcas simulacao;

    /** Roda a física a cada quadro enquanto ela tiver energia. */
    private final AnimationTimer relogio = new AnimationTimer() {
        @Override
        public void handle(long agora) {
            if (simulacao != null && !simulacao.parada()) {
                simulacao.passo(arrastado);
                simulacao.aplicarNosCards(arrastado);
            }
        }
    };

    public TrilhaView(Trilha trilha) {
        this.trilha = trilha;
        getStyleClass().add("trilha");

        Rectangle recorte = new Rectangle();
        recorte.widthProperty().bind(widthProperty());
        recorte.heightProperty().bind(heightProperty());
        setClip(recorte);

        // O mundo é desenhado em coordenadas próprias; deslocamento e escala fazem a câmera.
        mundo.getTransforms().addAll(deslocamento, escala);
        getChildren().addAll(tela, mundo);

        for (Card card : trilha.cards()) {
            CardView view = new CardView(card);
            permitirArrastar(view, card);
            card.propriedades().forEach(p -> p.aoMudar(this::atualizarEstados));
            views.put(card, view);
            mundo.getChildren().add(view);
        }
        trilha.conexoes().forEach(this::desenharConexao);
        trilha.links().forEach(this::desenharLink);
        atualizarEstados();

        setOnScroll(this::zoom);
        setOnMousePressed(e -> {
            arrasteX = e.getX() - deslocamento.getX();
            arrasteY = e.getY() - deslocamento.getY();
        });
        setOnMouseDragged(e -> {
            deslocamento.setX(e.getX() - arrasteX);
            deslocamento.setY(e.getY() - arrasteY);
        });

        widthProperty().addListener(o -> desenharFundo());
        heightProperty().addListener(o -> desenharFundo());
        deslocamento.xProperty().addListener(o -> desenharFundo());
        deslocamento.yProperty().addListener(o -> desenharFundo());
        escala.xProperty().addListener(o -> desenharFundo());
        fundo.addListener(o -> desenharFundo());
        relogio.start();
    }

    /**
     * Arruma os cards com a disposição escolhida. Os cards deslizam até o lugar novo e a câmera
     * centraliza o conteúdo, para não perder de vista onde cada coisa estava.
     */
    public void aplicar(Disposicao disposicao) {
        if (disposicao.viva()) {
            // Física: a simulação parte das posições atuais e roda a cada quadro (ver relogio).
            simulacao = new SimulacaoDeForcas(trilha);
            return;
        }
        simulacao = null;
        Map<Card, Point2D> destino = disposicao.calcular(trilha, foco);
        if (destino.isEmpty()) {
            return;
        }

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        Timeline animacao = new Timeline();
        for (var entrada : destino.entrySet()) {
            Card card = entrada.getKey();
            Point2D p = entrada.getValue();
            CardView view = views.get(card);
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            maxX = Math.max(maxX, p.getX() + view.getWidth());
            maxY = Math.max(maxY, p.getY() + view.getHeight());
            animacao.getKeyFrames().add(new KeyFrame(ANIMACAO,
                    new KeyValue(card.xProperty(), p.getX(), Interpolator.EASE_BOTH),
                    new KeyValue(card.yProperty(), p.getY(), Interpolator.EASE_BOTH)));
        }
        double s = escala.getX();
        animacao.getKeyFrames().add(new KeyFrame(ANIMACAO,
                new KeyValue(deslocamento.xProperty(), getWidth() / 2 - (minX + maxX) / 2 * s, Interpolator.EASE_BOTH),
                new KeyValue(deslocamento.yProperty(), getHeight() / 2 - (minY + maxY) / 2 * s, Interpolator.EASE_BOTH)));
        animacao.play();
    }

    private void atualizarEstados() {
        views.forEach((card, view) -> view.atualizar(trilha.estadoDe(card)));
    }

    private void permitirArrastar(CardView view, Card card) {
        double[] inicio = new double[4]; // mouse x, mouse y, card x, card y
        view.setOnMousePressed(e -> {
            foco = card;
            arrastado = card;
            inicio[0] = e.getSceneX();
            inicio[1] = e.getSceneY();
            inicio[2] = card.xProperty().get();
            inicio[3] = card.yProperty().get();
            view.toFront();
            e.consume();
        });
        view.setOnMouseDragged(e -> {
            card.xProperty().set(inicio[2] + (e.getSceneX() - inicio[0]) / escala.getX());
            card.yProperty().set(inicio[3] + (e.getSceneY() - inicio[1]) / escala.getY());
            if (simulacao != null) {
                simulacao.aquecer(); // os vizinhos reagem enquanto o card se move
            }
            e.consume();
        });
        view.setOnMouseReleased(e -> arrastado = null);
    }

    /** Linha contínua da direita da origem até a esquerda do destino. */
    private void desenharConexao(Conexao conexao) {
        CardView origem = views.get(conexao.origem());
        CardView destino = views.get(conexao.destino());
        Line linha = new Line();
        linha.getStyleClass().add("conexao");
        linha.startXProperty().bind(origem.layoutXProperty().add(origem.widthProperty()));
        linha.startYProperty().bind(origem.layoutYProperty().add(origem.heightProperty().divide(2)));
        linha.endXProperty().bind(destino.layoutXProperty());
        linha.endYProperty().bind(destino.layoutYProperty().add(destino.heightProperty().divide(2)));
        mundo.getChildren().addFirst(linha);
    }

    /** Linha tracejada entre os centros, só visível de perto. */
    private void desenharLink(Link link) {
        CardView a = views.get(link.a());
        CardView b = views.get(link.b());
        Line linha = new Line();
        linha.getStyleClass().add("link");
        linha.startXProperty().bind(a.layoutXProperty().add(a.widthProperty().divide(2)));
        linha.startYProperty().bind(a.layoutYProperty().add(a.heightProperty().divide(2)));
        linha.endXProperty().bind(b.layoutXProperty().add(b.widthProperty().divide(2)));
        linha.endYProperty().bind(b.layoutYProperty().add(b.heightProperty().divide(2)));
        linha.visibleProperty().bind(escala.xProperty().greaterThanOrEqualTo(ZOOM_LINKS));
        mundo.getChildren().addFirst(linha);
    }

    /** Zoom em volta do mouse: o ponto sob o cursor fica parado. */
    private void zoom(ScrollEvent e) {
        double atual = escala.getX();
        double nova = Math.clamp(atual * (e.getDeltaY() > 0 ? 1.1 : 1 / 1.1), ZOOM_MIN, ZOOM_MAX);
        double fator = nova / atual;
        deslocamento.setX(e.getX() - (e.getX() - deslocamento.getX()) * fator);
        deslocamento.setY(e.getY() - (e.getY() - deslocamento.getY()) * fator);
        escala.setX(nova);
        escala.setY(nova);
    }

    private void desenharFundo() {
        double largura = getWidth();
        double altura = getHeight();
        tela.setWidth(largura);
        tela.setHeight(altura);
        GraphicsContext g = tela.getGraphicsContext2D();
        g.clearRect(0, 0, largura, altura);
        fundo.get().desenhar(g, largura, altura, deslocamento.getX(), deslocamento.getY(), escala.getX());
    }

    public ObjectProperty<Fundo> fundoProperty() {
        return fundo;
    }
}

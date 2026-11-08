package io.github.luanfrombrazil.secondbrain.grafo.lab;

import io.github.luanfrombrazil.secondbrain.grafo.Parametros;
import io.github.luanfrombrazil.secondbrain.grafo.tela.Exemplos;
import io.github.luanfrombrazil.secondbrain.grafo.tela.Fundo;
import io.github.luanfrombrazil.secondbrain.grafo.tela.TelaDoGrafo;
import io.github.luanfrombrazil.secondbrain.grafo.tela.TipoDeDisposicao;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/** O grafo com dados de exemplo e controles para ajustar a física ao vivo. */
public final class LaboratorioDoGrafo extends BorderPane {

    private final Parametros parametros = new Parametros();
    private final TelaDoGrafo tela = new TelaDoGrafo(parametros);
    private final VBox controles = new VBox(4);
    private final Label leitura = new Label();

    public LaboratorioDoGrafo() {
        setCenter(tela);
        montarControles();
        ScrollPane rolagem = new ScrollPane(controles);
        rolagem.setFitToWidth(true);
        rolagem.setPrefWidth(310);
        rolagem.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox painel = new VBox(rolagem);
        VBox.setVgrow(rolagem, Priority.ALWAYS);
        painel.getStyleClass().add("painel");
        setRight(painel);
        carregar(50);

        Timeline relogio = new Timeline(new KeyFrame(Duration.millis(250), e -> atualizarLeitura()));
        relogio.setCycleCount(Timeline.INDEFINITE);
        relogio.play();
    }

    private void carregar(int tamanho) {
        Exemplos.Exemplo exemplo = Exemplos.gerar(tamanho);
        tela.mostrar(exemplo.grafo(), exemplo.cartoes(), exemplo.pontuais());
    }

    private void montarControles() {
        controles.setStyle("-fx-padding: 12;");

        titulo("Exemplo");
        ComboBox<TipoDeDisposicao> disposicao = new ComboBox<>();
        disposicao.getItems().addAll(TipoDeDisposicao.values());
        disposicao.setValue(TipoDeDisposicao.DEPENDENCIA);
        disposicao.setMaxWidth(Double.MAX_VALUE);
        disposicao.setOnAction(e -> tela.reorganizar(disposicao.getValue()));

        ComboBox<Fundo> fundo = new ComboBox<>();
        fundo.getItems().addAll(Fundo.values());
        fundo.setValue(Fundo.QUADRICULADO);
        fundo.setMaxWidth(Double.MAX_VALUE);
        fundo.setOnAction(e -> tela.setFundo(fundo.getValue()));

        ComboBox<Integer> tamanho = new ComboBox<>();
        tamanho.getItems().addAll(10, 50, 200, 500, 2000);
        tamanho.setValue(50);
        tamanho.setMaxWidth(Double.MAX_VALUE);
        tamanho.setOnAction(e -> carregar(tamanho.getValue()));

        rotulo("Disposição");
        controles.getChildren().add(disposicao);
        rotulo("Fundo");
        controles.getChildren().add(fundo);
        rotulo("Número de cards");
        controles.getChildren().add(tamanho);

        Button reorganizar = new Button("Reorganizar");
        reorganizar.setOnAction(e -> tela.reorganizar(disposicao.getValue()));
        Button centralizar = new Button("Centralizar");
        centralizar.setOnAction(e -> tela.centralizar());
        Button copiar = new Button("Copiar valores");
        copiar.setOnAction(e -> {
            ClipboardContent conteudo = new ClipboardContent();
            conteudo.putString(parametros.comoTexto());
            Clipboard.getSystemClipboard().setContent(conteudo);
        });
        HBox botoes = new HBox(6, reorganizar, centralizar, copiar);
        botoes.setStyle("-fx-padding: 8 0 0 0;");
        controles.getChildren().add(botoes);

        caixa("Conexões", tela::setMostrarConexoes);
        caixa("Links (só de perto)", tela::setMostrarLinks);
        caixa("Agrupar por pasta", tela::setAgruparPorPasta);

        titulo("Por dependência");
        arrastavel("Folga entre colunas", 30, 400, () -> parametros.folgaHorizontal, v -> parametros.folgaHorizontal = v);
        arrastavel("Folga entre linhas", 5, 200, () -> parametros.folgaVertical, v -> parametros.folgaVertical = v);

        titulo("Radial");
        arrastavel("Distância entre anéis", 260, 900, () -> parametros.passoRadial, v -> parametros.passoRadial = v);

        titulo("Por forças");
        arrastavel("Repulsão", 1000, 80000, () -> parametros.repulsao, v -> parametros.repulsao = v);
        arrastavel("Força da mola", 0.01, 0.6, () -> parametros.mola, v -> parametros.mola = v);
        arrastavel("Comprimento da mola", 100, 900, () -> parametros.comprimentoMola, v -> parametros.comprimentoMola = v);
        arrastavel("Gravidade", 0, 0.12, () -> parametros.gravidade, v -> parametros.gravidade = v);
        arrastavel("Atrito (velocidade mantida)", 0.2, 0.95, () -> parametros.atrito, v -> parametros.atrito = v);
        arrastavel("Resfriamento", 0.002, 0.08, () -> parametros.resfriamento, v -> parametros.resfriamento = v);
        arrastavel("Atração da pasta", 0, 0.2, () -> parametros.forcaPasta, v -> parametros.forcaPasta = v);

        titulo("Card solto (clique duplo no vazio)");
        arrastavel("Raio magnético", 0, 240, () -> parametros.raioMagnetico, v -> parametros.raioMagnetico = v);

        titulo("Zoom semântico (escala de cada troca)");
        String[] nomes = {"Bolinha → miniatura", "Miniatura → afastado", "Afastado → médio", "Médio → próximo"};
        for (int i = 0; i < nomes.length; i++) {
            int indice = i;
            arrastavelDeZoom(nomes[i], () -> parametros.limitesDeZoom[indice], v -> parametros.limitesDeZoom[indice] = v);
        }

        titulo("Medidas");
        leitura.getStyleClass().add("leitura");
        controles.getChildren().add(leitura);
    }

    private void titulo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("titulo-do-grupo");
        controles.getChildren().add(label);
    }

    private void rotulo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("rotulo");
        controles.getChildren().add(label);
    }

    private void caixa(String texto, Consumer<Boolean> acao) {
        CheckBox caixa = new CheckBox(texto);
        caixa.setSelected(true);
        caixa.selectedProperty().addListener((o, a, valor) -> acao.accept(valor));
        controles.getChildren().add(caixa);
    }

    private void arrastavel(String texto, double min, double max, DoubleSupplier ler, Consumer<Double> gravar) {
        controleDeslizante(texto, min, max, ler, valor -> {
            gravar.accept(valor);
            tela.recalcular();
        });
    }

    private void arrastavelDeZoom(String texto, DoubleSupplier ler, Consumer<Double> gravar) {
        controleDeslizante(texto, 0.05, 1.5, ler, valor -> {
            gravar.accept(valor);
            tela.marcarSujo();
        });
    }

    private void controleDeslizante(String texto, double min, double max, DoubleSupplier ler, Consumer<Double> aoMudar) {
        Label rotulo = new Label();
        rotulo.getStyleClass().add("rotulo");
        Slider slider = new Slider(min, max, ler.getAsDouble());
        Runnable escrever = () -> rotulo.setText(texto + ": " + formatar(slider.getValue()));
        escrever.run();
        slider.valueProperty().addListener((o, a, valor) -> {
            escrever.run();
            aoMudar.accept(valor.doubleValue());
        });
        controles.getChildren().addAll(rotulo, slider);
    }

    private static String formatar(double valor) {
        return String.format(Locale.ROOT, valor >= 100 ? "%.0f" : valor >= 1 ? "%.1f" : "%.3f", valor);
    }

    private void atualizarLeitura() {
        leitura.setText(String.format(Locale.ROOT, "Zoom: %.0f%%%nNível: %s%nCards: %d / %d%nFPS: %.0f%nSimulando: %s",
                tela.camera().escala() * 100, tela.nivel(), tela.desenhados(), tela.totalDeCards(), tela.fps(),
                tela.simulando() ? "sim" : "não"));
    }
}

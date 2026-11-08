package io.github.luanfrombrazil.secondbrain.card;

import io.github.luanfrombrazil.secondbrain.propriedade.Propriedade;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** O card desenhado no grafo: faixa de estado, título, propriedades e rodapé. */
public class CardView extends VBox {

    private final Card card;
    private final Region faixa = new Region();
    private final ProgressBar barra = new ProgressBar();

    public CardView(Card card) {
        this.card = card;
        getStyleClass().add("card");
        layoutXProperty().bind(card.xProperty());
        layoutYProperty().bind(card.yProperty());

        faixa.getStyleClass().add("faixa");

        Label titulo = new Label();
        titulo.textProperty().bind(card.tituloProperty());
        titulo.getStyleClass().add("titulo");

        getChildren().addAll(faixa, titulo);
        for (Propriedade propriedade : card.propriedades()) {
            Node visao = propriedade.visaoNoCard();
            if (visao != null) {
                getChildren().add(visao);
            }
        }
        getChildren().add(rodape());
    }

    private HBox rodape() {
        HBox rodape = new HBox();
        rodape.getStyleClass().add("rodape");
        if (card.progresso().isPresent()) {
            HBox.setHgrow(barra, Priority.ALWAYS);
            barra.setMaxWidth(Double.MAX_VALUE);
            rodape.getChildren().add(barra);
        }
        TipoDeCard tipo = card.tipoProperty().get();
        if (tipo != null) {
            Label icone = new Label(tipo.icone());
            icone.getStyleClass().add("icone-tipo");
            rodape.getChildren().add(icone);
        }
        return rodape;
    }

    /** Redesenha o que depende do estado e do progresso. */
    public void atualizar(Estado estado) {
        faixa.getStyleClass().removeIf(c -> c.startsWith("estado-"));
        faixa.getStyleClass().add("estado-" + estado.name().toLowerCase());
        card.progresso().ifPresent(barra::setProgress);
    }
}

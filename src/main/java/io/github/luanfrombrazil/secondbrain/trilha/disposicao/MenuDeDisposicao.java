package io.github.luanfrombrazil.secondbrain.trilha.disposicao;

import io.github.luanfrombrazil.secondbrain.trilha.TrilhaView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.List;

/** Os botões para escolher a disposição. Clicar de novo reaplica (útil no radial, com outro foco). */
public class MenuDeDisposicao extends HBox {

    private static final List<Disposicao> DISPOSICOES = List.of(new PorDependencia(), new PorForcas(), new Radial());

    public MenuDeDisposicao(TrilhaView trilha) {
        getStyleClass().add("menu");
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        ToggleGroup grupo = new ToggleGroup();
        for (Disposicao disposicao : DISPOSICOES) {
            ToggleButton botao = new ToggleButton(disposicao.nome());
            botao.setToggleGroup(grupo);
            botao.setOnAction(e -> {
                botao.setSelected(true); // o escolhido continua marcado mesmo se clicado de novo
                trilha.aplicar(disposicao);
            });
            getChildren().add(botao);
            if (disposicao.viva()) {
                // O app abre com a física ligada.
                botao.setSelected(true);
                trilha.aplicar(disposicao);
            }
        }
    }
}

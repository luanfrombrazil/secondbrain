package io.github.luanfrombrazil.secondbrain.trilha.fundo;

import io.github.luanfrombrazil.secondbrain.trilha.TrilhaView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.List;

/** Os botões para escolher o fundo. */
public class MenuDeFundo extends HBox {

    private static final List<Fundo> FUNDOS = List.of(new Liso(), new Pautado(), new Pontilhado(), new Quadriculado());

    public MenuDeFundo(TrilhaView trilha) {
        getStyleClass().add("menu");
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        ToggleGroup grupo = new ToggleGroup();
        for (Fundo fundo : FUNDOS) {
            ToggleButton botao = new ToggleButton(fundo.nome());
            botao.setToggleGroup(grupo);
            botao.setSelected(fundo.getClass() == trilha.fundoProperty().get().getClass());
            botao.setOnAction(e -> {
                botao.setSelected(true);
                trilha.fundoProperty().set(fundo);
            });
            getChildren().add(botao);
        }
    }
}

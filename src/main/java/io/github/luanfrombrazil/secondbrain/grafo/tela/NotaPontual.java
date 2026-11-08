package io.github.luanfrombrazil.secondbrain.grafo.tela;

import java.util.List;

/** Nota de peso baixo: fica fora da disposição e só aparece, de perto, junto do card em foco. */
public record NotaPontual(String titulo, List<String> ligadaA) {
}

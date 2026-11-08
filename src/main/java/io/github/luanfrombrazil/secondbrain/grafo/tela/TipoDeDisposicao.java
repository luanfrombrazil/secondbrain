package io.github.luanfrombrazil.secondbrain.grafo.tela;

import io.github.luanfrombrazil.secondbrain.grafo.Disposicao;
import io.github.luanfrombrazil.secondbrain.grafo.PorDependencia;
import io.github.luanfrombrazil.secondbrain.grafo.PorForcas;
import io.github.luanfrombrazil.secondbrain.grafo.Radial;

public enum TipoDeDisposicao {
    DEPENDENCIA("Por dependência", new PorDependencia()),
    FORCAS("Por forças", new PorForcas()),
    RADIAL("Radial", new Radial());

    private final String nome;
    private final Disposicao disposicao;

    TipoDeDisposicao(String nome, Disposicao disposicao) {
        this.nome = nome;
        this.disposicao = disposicao;
    }

    public Disposicao disposicao() {
        return disposicao;
    }

    @Override
    public String toString() {
        return nome;
    }
}

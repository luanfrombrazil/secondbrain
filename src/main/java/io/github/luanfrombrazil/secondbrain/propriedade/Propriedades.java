package io.github.luanfrombrazil.secondbrain.propriedade;

import io.github.luanfrombrazil.secondbrain.propriedade.progresso.Progresso;
import io.github.luanfrombrazil.secondbrain.propriedade.tarefas.Tarefas;
import io.github.luanfrombrazil.secondbrain.propriedade.texto.Texto;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** O registro das propriedades disponíveis. Uma propriedade nova entra aqui com uma linha. */
public final class Propriedades {

    private static final Map<String, Supplier<Propriedade>> REGISTRO = new LinkedHashMap<>();

    static {
        registrar("tarefas", Tarefas::new);
        registrar("progresso", Progresso::new);
        registrar("texto", () -> new Texto("Campo", ""));
    }

    private Propriedades() {
    }

    public static void registrar(String id, Supplier<Propriedade> fabrica) {
        REGISTRO.put(id, fabrica);
    }

    public static Propriedade criar(String id) {
        Supplier<Propriedade> fabrica = REGISTRO.get(id);
        if (fabrica == null) {
            throw new IllegalArgumentException("Propriedade desconhecida: " + id);
        }
        return fabrica.get();
    }

    public static Set<String> disponiveis() {
        return REGISTRO.keySet();
    }
}

package io.github.luanfrombrazil.secondbrain.card;

import java.util.List;

/**
 * Um preset inicial. O nome e o ícone são compartilhados pelos cards do tipo;
 * as propriedades são só copiadas quando o card é criado (ver {@link Card#doTipo}).
 *
 * @param propriedadesIniciais ids do registro de {@code Propriedades}, como "tarefas"
 */
public record TipoDeCard(String nome, String icone, List<String> propriedadesIniciais) {
}

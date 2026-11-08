package io.github.luanfrombrazil.secondbrain.grafo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Tira de baixo dos cards fixos o que estiver perto, e separa o que se encostar por causa disso. */
final class AfastamentoMagnetico {

    private static final int RODADAS = 40;
    private static final double FOLGA_ENTRE_CARDS = 16;

    private AfastamentoMagnetico() {
    }

    static Map<String, Ponto> aplicar(Map<String, Ponto> posicoes, Set<String> fixos, double raio) {
        if (fixos.isEmpty()) {
            return posicoes;
        }
        Map<String, Ponto> saida = new HashMap<>(posicoes);
        Set<String> empurrados = new HashSet<>();
        double meiaLargura = No.LARGURA + raio;
        double meiaAltura = No.ALTURA + raio;

        for (String fixo : fixos) {
            Ponto centro = saida.get(fixo);
            for (var entrada : posicoes.entrySet()) {
                String id = entrada.getKey();
                if (fixos.contains(id)) {
                    continue;
                }
                Ponto atual = saida.get(id);
                double dx = atual.x() - centro.x();
                double dy = atual.y() - centro.y();
                if (Math.abs(dx) >= meiaLargura || Math.abs(dy) >= meiaAltura) {
                    continue;
                }
                if (dx == 0 && dy == 0) {
                    dx = 1;
                }
                double t = Math.min(
                        dx == 0 ? Double.MAX_VALUE : meiaLargura / Math.abs(dx),
                        dy == 0 ? Double.MAX_VALUE : meiaAltura / Math.abs(dy));
                saida.put(id, new Ponto(centro.x() + dx * t, centro.y() + dy * t));
                empurrados.add(id);
            }
        }
        separar(saida, fixos, empurrados);
        return saida;
    }

    /** O card empurrado cede; quem ele encostar também passa a ceder. */
    private static void separar(Map<String, Ponto> pos, Set<String> fixos, Set<String> empurrados) {
        double minX = No.LARGURA + FOLGA_ENTRE_CARDS;
        double minY = No.ALTURA + FOLGA_ENTRE_CARDS;
        for (int rodada = 0; rodada < RODADAS; rodada++) {
            boolean mexeu = false;
            for (String a : List.copyOf(empurrados)) {
                for (String b : new ArrayList<>(pos.keySet())) {
                    if (a.equals(b)) {
                        continue;
                    }
                    Ponto pa = pos.get(a);
                    Ponto pb = pos.get(b);
                    double dx = pa.x() - pb.x();
                    double dy = pa.y() - pb.y();
                    double sobraX = minX - Math.abs(dx);
                    double sobraY = minY - Math.abs(dy);
                    if (sobraX <= 0 || sobraY <= 0) {
                        continue;
                    }
                    if (sobraY < sobraX) {
                        double sinal = dy >= 0 ? 1 : -1;
                        pos.put(a, new Ponto(pa.x(), pa.y() + sinal * sobraY));
                    } else {
                        double sinal = dx >= 0 ? 1 : -1;
                        pos.put(a, new Ponto(pa.x() + sinal * sobraX, pa.y()));
                    }
                    if (!fixos.contains(b)) {
                        empurrados.add(b);
                    }
                    mexeu = true;
                }
            }
            if (!mexeu) {
                return;
            }
        }
    }
}

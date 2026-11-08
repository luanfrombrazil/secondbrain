package io.github.luanfrombrazil.secondbrain.grafo.tela;

import io.github.luanfrombrazil.secondbrain.grafo.Aresta;
import io.github.luanfrombrazil.secondbrain.grafo.Grafo;
import io.github.luanfrombrazil.secondbrain.grafo.No;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Gera grafos de exemplo para o laboratório. O mesmo tamanho sempre gera o mesmo grafo. */
public final class Exemplos {

    public record Exemplo(Grafo grafo, Map<String, Cartao> cartoes, List<NotaPontual> pontuais) {
    }

    private static final String[][] AREAS = {
            {"Matemática", "Cálculo", "Álgebra Linear", "EDO", "EDP", "Estatística", "Séries"},
            {"Programação", "Programação", "Algoritmos", "Banco de Dados", "Compiladores", "POO"},
            {"Hardware", "Circuitos", "Sistemas Digitais", "Arquitetura", "Sistemas Operacionais"},
            {"Redes", "Redes", "Sistemas Distribuídos", "Segurança"},
            {"IA e dados", "Ciência de Dados", "Inteligência Artificial", "Otimização"},
    };
    private static final double PARTE_DE_SOLTOS = 0.06;
    private static final double CHANCE_DE_SEGUNDO_PAI = 0.25;
    private static final double CHANCE_DE_LINK = 0.15;
    private static final int MAX_FILHOS = 3;

    private Exemplos() {
    }

    public static Exemplo gerar(int tamanho) {
        Random sorte = new Random(42);
        List<No> nos = new ArrayList<>();
        List<Aresta> conexoes = new ArrayList<>();
        List<Aresta> links = new ArrayList<>();
        Map<String, Cartao> cartoes = new LinkedHashMap<>();
        List<List<String>> porArea = new ArrayList<>();
        Map<String, Integer> filhosDe = new LinkedHashMap<>();
        for (int i = 0; i < AREAS.length; i++) {
            porArea.add(new ArrayList<>());
        }

        int soltos = (int) Math.ceil(tamanho * PARTE_DE_SOLTOS);
        for (int i = 0; i < tamanho; i++) {
            String id = String.format("c%04d", i);
            filhosDe.put(id, 0);
            boolean solto = i >= tamanho - soltos;
            int area = i % AREAS.length;
            String[] nomes = AREAS[area];
            String titulo = nomes[1 + i % (nomes.length - 1)] + " " + romano(porArea.get(area).size() / (nomes.length - 1) + 1);
            nos.add(new No(id, solto ? null : nomes[0], null));
            cartoes.put(id, cartao(id, titulo, sorte));

            if (!solto) {
                List<String> mesmaArea = porArea.get(area);
                String pai = escolherPai(mesmaArea, filhosDe, sorte);
                if (pai != null) {
                    conexoes.add(new Aresta(pai, id));
                    filhosDe.merge(pai, 1, Integer::sum);
                }
                if (i > AREAS.length && sorte.nextDouble() < CHANCE_DE_SEGUNDO_PAI) {
                    String outro = String.format("c%04d", sorte.nextInt(i));
                    boolean repetido = outro.equals(pai);
                    boolean ehSolto = nos.stream().anyMatch(n -> n.id().equals(outro) && n.pasta() == null);
                    if (!repetido && !ehSolto) {
                        conexoes.add(new Aresta(outro, id));
                    }
                }
                if (i > 2 && sorte.nextDouble() < CHANCE_DE_LINK) {
                    links.add(new Aresta(String.format("c%04d", sorte.nextInt(i)), id));
                }
                mesmaArea.add(id);
            }
        }

        List<NotaPontual> pontuais = new ArrayList<>();
        for (int i = 0; i < tamanho; i += 2) {
            String id = String.format("c%04d", i);
            pontuais.add(new NotaPontual("O Andar do Bêbado", List.of(id)));
            pontuais.add(new NotaPontual("Artigo " + (i + 1) + ".pdf", List.of(id)));
        }
        return new Exemplo(new Grafo(nos, conexoes, links), cartoes, pontuais);
    }

    private static String escolherPai(List<String> candidatos, Map<String, Integer> filhosDe, Random sorte) {
        List<String> livres = candidatos.stream().filter(c -> filhosDe.get(c) < MAX_FILHOS).toList();
        if (livres.isEmpty()) {
            return null;
        }
        // Prefere os mais recentes, para as árvores ganharem profundidade.
        int recentes = Math.min(livres.size(), 4);
        return livres.get(livres.size() - 1 - sorte.nextInt(recentes));
    }

    private static Cartao cartao(String id, String titulo, Random sorte) {
        double progresso;
        Color cor;
        switch (sorte.nextInt(4)) {
            case 0 -> {
                cor = DesenhoDoGrafo.COR_CONCLUIDO;
                progresso = 1;
            }
            case 1 -> {
                cor = DesenhoDoGrafo.COR_EM_ANDAMENTO;
                progresso = 0.2 + sorte.nextDouble() * 0.7;
            }
            case 2 -> {
                cor = DesenhoDoGrafo.COR_DISPONIVEL;
                progresso = 0;
            }
            default -> {
                cor = DesenhoDoGrafo.COR_BLOQUEADO;
                progresso = 0;
            }
        }
        List<String> tarefas = List.of("Lista 1", "Lista 2", "Revisão");
        int feitas = (int) Math.round(progresso * tarefas.size());
        return new Cartao(id, titulo, cor, progresso, 0.2 + sorte.nextDouble() * 0.8, tarefas, feitas, sorte.nextInt(4) == 0);
    }

    private static String romano(int n) {
        String[] algarismos = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return n <= algarismos.length ? algarismos[n - 1] : String.valueOf(n);
    }
}

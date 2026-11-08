# Implementação

A ordem em que o app é construído. Cada fase só começa depois de a anterior estar aprovada.

| Fase | Entrega |
|---|---|
| **0. Base** | Branch `dev`, `.gitignore`, projeto Maven novo, JUnit |
| **1. Laboratório do grafo** | Dados de exemplo e controles para ajustar ao vivo as disposições, a física e o zoom semântico |
| **2. Núcleo** | Acervo e `.secondbrain/` em YAML, modelo novo, bloqueios, ciclos, religar a cadeia. O grafo passa a usar dados reais, com explorador, criar card e conexão, e desfazer |
| **3. Notas** | Só `.tex`: editor, Tectonic embutido, PDF no papel lateral, preâmbulo comum |
| **4. Ligações** | Links e `\nota`, notas fantasmas, arquivos no grafo, mudanças por fora, estado de erro |
| **5. Módulos** | Um por vez: editor do card e propriedades, kanban, lista e filtros, calendário, bibliografia e compor documento, SyncTeX e EPUB, troca rápida e paleta, instalador |
| **Depois do app pronto** | Atualização com compatibilidade com `.md` |

## Branches

O trabalho das fases 0 a 4 fica em `dev`. A partir da fase 5, cada módulo sai de `dev` num branch próprio.

## Rodar

```
mvn javafx:run
```

No laboratório: arrastar move a tela, a roda do mouse dá zoom, clique duplo no vazio cria um card solto. **Copiar valores** copia os parâmetros atuais, que viram os padrões depois de aprovados.

Para gerar imagens da tela em `target/capturas/`:

```
mvn test -Dcaptura=true -Dtest=CapturaDeTelaTest
```

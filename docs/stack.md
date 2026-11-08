# Stack

O SecondBrain é um app de desktop em **Java 25** com **JavaFX**, construído com **Maven**.

## Versões

| Item | Versão | Onde está |
|---|---|---|
| Java | 25 | `maven.compiler.release` no `pom.xml` |
| JavaFX (OpenJFX) | 27.0.1, só o `javafx-controls` | `javafx.version` no `pom.xml` |
| Maven Compiler Plugin | 3.16.0 | `pom.xml` |
| JavaFX Maven Plugin | 0.0.8 | `pom.xml` |

## Rodar

```
mvn javafx:run
```

- A classe principal é `io.github.luanfrombrazil.secondbrain.app.SecondBrainApp`.
- O plugin passa `--enable-native-access=javafx.graphics`, para o JavaFX usar acesso nativo sem aviso no Java 25.
- O projeto é um módulo Java (`module-info.java`): `io.github.luanfrombrazil.secondbrain`, que requer `javafx.controls` e exporta o pacote `app` para o `javafx.graphics`.
- O visual vem de CSS, em `src/main/resources/.../app/secondbrain.css`.

## Pacotes

Os pacotes seguem as partes do produto, com o modelo e a tela juntos, e não camadas técnicas.

**O código é todo em inglês** (pacotes, classes, variáveis e comentários), por exemplo `CardType` para tipo de card. O texto que o usuário vê continua em português.

| Pacote | O que tem |
|---|---|
| `app` | Inicialização e janela (`SecondBrainApp`) |
| `track`, `card`, `property` | O modelo: trilhas e regras de estado, cards e propriedades |
| `vault` | O acervo no disco: `Vault`, `Slug` e os arquivos YAML |
| `graph` | O grafo, sem JavaFX: `Graph`, `Node`, `Edge` e `Point` |
| `graph.layout` | O contrato das disposições: `Layout`, `AnimatedLayout`, `LayoutRun`, `Tunable` e a lista `Layouts` |
| `graph.layout.dependency`, `.radial`, `.force` | Uma disposição em cada pacote, com os próprios parâmetros. Uma não importa a outra |
| `graph.layout.common` | O que as disposições em árvore reaproveitam: `Forest`, `LooseCards` e `MagneticRepulsion` |
| `graph.view` | A tela infinita: `GraphView`, `GraphRenderer` e `ViewParameters` |
| `graph.view.camera` | `Camera` e `ZoomLevel` |
| `graph.view.data` | O que o grafo mostra de cada card: `CardData` e `PointNote` |
| `graph.view.backdrop` | O contrato dos fundos: `Backdrop` e a lista `Backdrops`. `.plain`, `.lined`, `.dotted` e `.grid` têm um padrão cada, e `.common` tem a grade de dois níveis que eles reaproveitam |
| `graph.lab` | O laboratório para ajustar a física (`GraphLab`) e os exemplos (`Samples`) |

Os próximos pacotes nascem com as fases de [implementacao.md](implementacao.md): `note` (as notas, fase 3) e uma visualização por pacote, como o `graph`: `kanban`, `list` e `calendar` (fase 5). Cada disposição e cada fundo é um pacote próprio, plugável pela sua lista (`Layouts`, `Backdrops`). O laboratório (`graph.lab`) continua como uma tela à parte, fora do app.

## Dependências

O app usa o `javafx-controls` e o SnakeYAML Engine (arquivos de `.secondbrain/`), e os testes usam o JUnit. As disposições, inclusive a simulação de forças, são implementadas no próprio projeto.

## O que ainda não tem biblioteca

Peças que vão pedir uma escolha quando chegarem. Os nomes são candidatos, não decisões.

| Peça | Candidatos |
|---|---|
| Ler e mostrar Markdown | commonmark-java (com `SourceSpans`, para cada bloco do HTML guardar a linha do `.md` e o clique duplo voltar ao editor), flexmark-java. O HTML aparece no `WebView` do JavaFX (`javafx-web`). |
| Tradutor LaTeX ↔ Markdown (futuro) | **Pandoc**, executável externo embutido no app como o Tectonic: converte `.md` para LaTeX (para o documento composto) e `.tex` para Markdown. Só traduz bem o LaTeX padrão, que é o que as notas usam (D-12). |
| Fórmulas no Markdown | KaTeX carregado dentro do `WebView` |
| Gerar o PDF de um `.md` | A partir do mesmo HTML: openhtmltopdf (usa o PDFBox), ou a impressão do `WebEngine` para PDF |
| Editor de `.md` e `.tex` com destaque de sintaxe | RichTextFX |
| Compilar o LaTeX | Tectonic (executável externo), ou o TeX instalado na máquina |
| Mostrar o PDF e gerar as miniaturas | Apache PDFBox |
| Ler `.epub` | O EPUB é um `.zip` com XHTML, CSS e um índice (OPF). Opções: abrir com `java.util.zip` e ler o OPF no próprio projeto, ou usar a epublib; mostrar os capítulos no `WebView` do JavaFX (pede o módulo `javafx-web`, que hoje não está no `pom.xml`). |
| Do PDF para o `.tex` (clique duplo no PDF abre o editor no mesmo ponto) | SyncTeX: o Tectonic gera o `.synctex.gz` com `--synctex`, e o app lê esse arquivo para achar o arquivo e a linha a partir da página e da posição do clique |
| Fórmulas instantâneas | JLaTeXMath |
| Vigiar a pasta para mudanças externas | `java.nio.file.WatchService`, do próprio Java |
| Sincronizar com o Google Calendar (futuro) | **Google Calendar API v3** (`google-api-services-calendar`), com OAuth de app de desktop (`google-oauth-java-client`, login no navegador e volta por `localhost`). Sincroniza só o que mudou com `syncToken`. O token de acesso fica no cofre do sistema (Credential Manager do Windows), nunca no acervo. Precisa de um projeto no Google Cloud, e o escopo de calendário pede a verificação do Google para sair do modo de teste. |
| Datas no padrão iCalendar | **ical4j**: lê e escreve `VEVENT`, `RRULE` e fusos. Também expande a recorrência para o calendário do app. Com ele, exportar e importar `.ics` sai quase de graça, sem conta nenhuma. Os fusos vêm do `java.time` (`ZoneId`). |
| Alternativa sem login (futuro) | Importar pelo endereço secreto iCal de um calendário do Google. É só leitura, e o Google demora horas para atualizar um calendário assinado. |
| Testes da interface | TestFX |

## Cuidados

- **Thread da interface.** Compilar, renderizar PDF e calcular arranjos pesados rodam fora da thread do JavaFX, e o resultado volta por `Platform.runLater`.
- **Quantidade de nós.** Com muitos cards, o zoom semântico ajuda: de longe, cada card vira uma miniatura simples.
- **Texto no zoom.** Fonte escalada pode borrar. Trocar o desenho do card por nível de zoom, em vez de só escalar, evita isso.

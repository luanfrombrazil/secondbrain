# Stack

> A stack já está decidida: **Java 25, JavaFX e Maven**. Este documento registra a escolha e mostra como cada peça do app se encaixa nela. As bibliotecas citadas são sugestões, ainda não adotadas, salvo quando dito.

## Decisão

| Item | Escolha |
|---|---|
| Linguagem | **Java 25** (versão LTS) |
| Interface | **JavaFX** (OpenJFX), com o grafo de cena para a tela infinita |
| Build | **Maven**, com o `javafx-maven-plugin` (`org.openjfx`) para rodar e empacotar |
| Plataforma | App de desktop |

## Por que essa stack serve ao Trilhas

- **O grafo de cena resolve o ponto crítico.** A [viabilidade](viabilidade.md) aponta a tela infinita como a parte mais arriscada. No JavaFX, cada card é um nó com transformações (`Scale`, `Translate`), descoberta do que está sob o mouse e eventos de arrastar já prontos. Não é preciso desenhar tudo à mão.
- **O ecossistema Java tem as peças difíceis.** O ELK (layout em camadas) é uma biblioteca Java, e há bibliotecas maduras para PDF e fórmulas.
- **Tem 3D nativo.** O JavaFX traz `SubScene`, `PerspectiveCamera` e formas 3D, o que deixa a porta aberta para as camadas empilhadas (D-04) sem trocar de tecnologia.
- **Empacota como app comum.** `jlink` e `jpackage` geram um instalador com o próprio runtime, sem pedir que a pessoa instale Java.

## Peça por peça

| Peça | Como fazer em JavaFX | Biblioteca sugerida |
|---|---|---|
| Tela infinita (arrastar, zoom, conexões) | Um `Pane` de conteúdo com `Scale` e `Translate` controlados pela câmera; conexões como `CubicCurve` ou `Path` | Nenhuma |
| Zoom semântico | Trocar o nó de cada card conforme a escala (bolinha, afastado, médio, próximo) e esconder os nós com peso abaixo do nível | Nenhuma |
| Muitos cards no zoom afastado | Desenhar as bolinhas num `Canvas` em vez de milhares de nós, se o desempenho pedir | Nenhuma |
| Auto-organização (Sugiyama) | Rodar o layout em camadas e aplicar as posições aos nós | ELK (`org.eclipse.elk`) |
| Grafo de bolinhas com clusters | Layout por forças | ELK (algoritmo de forças) ou implementação própria |
| Lógica de dependências e estados | Grafo dirigido no modelo, com ordem topológica e detecção de ciclos | Nenhuma (ou JGraphT) |
| Editor `.tex` com destaque de sintaxe | `CodeArea` com estilos por expressão regular | RichTextFX |
| Compilar o LaTeX | Chamar o Tectonic (ou `pdflatex`/`latexmk`) por `ProcessBuilder`, fora da thread da interface | Tectonic (executável externo) |
| Mostrar o PDF e gerar as miniaturas | Renderizar cada página como imagem e exibir num `ImageView` | Apache PDFBox |
| Fórmulas instantâneas | Renderizar só a fórmula sob o cursor | JLaTeXMath |
| Links entre notas e mudanças externas | Ler os `\href` dos `.tex` e vigiar a pasta | `java.nio.file.WatchService` |
| Sincronia `.tex` ↔ PDF (SyncTeX) | Ler o `.synctex.gz` à mão | Nenhuma pronta em Java |
| 3D | `SubScene` com `PerspectiveCamera`, uma camada por plano | JavaFX 3D |

## Cuidados

- **Thread da interface.** Compilação, renderização de PDF e layout do ELK rodam em segundo plano (`Task`, `ExecutorService` ou threads virtuais do Java 25), e o resultado volta pela `Platform.runLater`.
- **Quantidade de nós.** Mil cards completos ao mesmo tempo pesam. O zoom semântico ajuda, porque de longe cada card vira um nó simples ou um ponto no `Canvas`.
- **Texto em vários zooms.** Fontes escaladas por `Scale` podem ficar borradas. Trocar o nó por nível de zoom, em vez de só escalar, evita isso.
- **Módulos.** O JavaFX funciona melhor com o projeto como módulo (`module-info.java`), o que também facilita o `jlink`.

## Em aberto

- Qual compilador LaTeX vem por padrão: o Tectonic embutido ou o TeX instalado na máquina.
- Onde ficam os dados da nota que não são texto (ver [ideias em aberto](ideias-em-aberto.md)); a stack não muda essa decisão.
- Biblioteca de testes da interface (por exemplo, TestFX).

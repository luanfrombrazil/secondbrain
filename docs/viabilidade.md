# Viabilidade técnica

> Esta análise foi feita quando a ideia ainda era parte do Overtimer (Java e Swing). A stack do projeto independente já foi decidida: Java 25, JavaFX e Maven. Ver [stack](stack.md) para como cada peça abaixo se encaixa nela.

## Peça por peça

| Peça | Dificuldade | Observação |
|---|---|---|
| Lógica de dependências e estados | Baixa | É um grafo dirigido: ordem topológica e detecção de ciclos. |
| Tela infinita com cards, arrastar, zoom e conexões | **Média para alta** | É o ponto mais arriscado. Veja abaixo. |
| Zoom semântico | Média | Depois que a tela existe, é uma regra de visibilidade por nível de zoom e peso do card. |
| Auto-organização | Média | Layout em camadas (Sugiyama). A biblioteca ELK (Eclipse Layout Kernel) faz isso. Difícil é conviver com o posicionamento manual. |
| Grafo de bolinhas com clusters | Média | Layout por forças, com atração por área. |
| Trilha interna | Baixa | A mesma tela, recursiva no modelo de dados. |
| Editor `.tex` com destaque de sintaxe | Baixa | Há componentes prontos com modo LaTeX. |
| Compilar o LaTeX | Média | Exige um TeX. O Tectonic é um executável único que baixa os pacotes sob demanda e funciona offline depois. A alternativa é usar um TeX Live ou MiKTeX instalado. |
| Mostrar o PDF e gerar as miniaturas | Baixa | Bibliotecas de renderização de PDF transformam páginas em imagem. |
| Sincronia `.tex` ↔ PDF (SyncTeX) | Média para alta | O Tectonic gera o arquivo, mas a leitura pode ter de ser feita à mão. Pode ficar para depois. |
| Fórmulas instantâneas fora do PDF | Baixa | Bibliotecas de renderização de fórmulas desenham só a fórmula. |
| Links entre notas | Baixa | Ler o `\href` dos `.tex` e vigiar a pasta para mudanças externas. |
| 3D de verdade | Alta | Fase separada, bem mais à frente. |

## O ponto crítico: a tela infinita

- **Desenhar tudo à mão num único painel** (câmera, descobrir o que está sob o mouse, arrastar, texto em vários níveis de zoom, curvas das conexões) é viável. Muitos editores de grafo fazem assim, mas é a maior fatia do trabalho.
- **Alternativas** que já trazem transformações e zoom prontos: um toolkit com grafo de cena (como o JavaFX) ou uma interface web com uma biblioteca de grafos.
- **A escolha da tecnologia da tela** é a decisão que mais pesa no custo de tudo o que vem depois, inclusive no 3D.

## O que pode ser adiado sem prejudicar o resto

- SyncTeX.
- Edição dentro do app. No começo, o `.tex` pode abrir num editor externo, e a app só mostra o PDF.
- 3D.

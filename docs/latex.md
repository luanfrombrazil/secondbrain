# LaTeX

## Regra principal: só LaTeX padrão

O `.tex` de uma nota precisa compilar **fora do programa**, em qualquer distribuição LaTeX, **sem pacote ou classe próprios**.

- **Não usar comandos inventados**, como `\nota{}`, `\tarefa{}`, `\card{}`, `\livro{}`, `\evento{}` ou `\usepackage{trilhas}`.
- **Usar o que é padrão:**
  - `\section` para a estrutura;
  - `itemize` e `enumerate` para listas;
  - `\cite` e `\nocite` com um `.bib` ao lado;
  - `\href` do `hyperref` para ligar notas, de preferência apontando para o PDF da outra nota (`\href{regra-da-cadeia.pdf}{Regra da cadeia}`), o que funciona como link clicável mesmo fora do app;
  - comentários `%`.
- **Título e autor:** `\title{Derivadas parciais}` e `\author{Luan Lana Maia}`. O autor é a pessoa, nunca o card. A ligação entre a nota e o card fica a cargo da app.

Exemplo do cabeçalho de uma nota:

```latex
\documentclass{article}
\usepackage{amsmath}
\usepackage{hyperref}

\title{Cálculo II}
\author{Luan Lana Maia}

\begin{document}
\maketitle
...
\end{document}
```

## Aparência

- O `article` do LaTeX, com a **fonte Computer Modern**. No Figma, a fonte de protótipo é a Old Standard TT, porque a CMU Serif não está instalada.
- **Elementos do `article`:** título centralizado, autor e data; seções numeradas em negrito; texto justificado; variáveis em itálico matemático; equações centralizadas e numeradas à direita; `itemize` com marcadores; links em azul.
- **Folha:** papel branco, proporção A4.

## Leitura

- **Por padrão, a nota aparece como o PDF compilado.** Notas são muito mais lidas do que escritas.
- **No grafo,** clicar num card faz um pedaço do papel **espiar** pela borda direita, com uma aba **<**. Clicar na aba abre a nota **ao lado do grafo**, com uma sombra forte separando o papel dos cards.
- **Abas do papel:**
  - **<** abre a **página dupla**, com duas páginas lado a lado como um livro, separadas só por um espaço de alguns milímetros (sem sombra na lombada);
  - **>** recolhe, um passo de cada vez.
- **No modal do card,** o estado **Nota** mostra a folha em tela cheia.

## Edição

- **Modo edição:** o `.tex` à esquerda e o PDF à direita.
- **Interface única:** editor do card, `.tex` e PDF lado a lado.
- **Barra do editor**, em grupos:
  - desfazer e refazer;
  - estilo do texto (com menu);
  - negrito e itálico;
  - matemática e símbolos (Ω);
  - link, comentário, rótulo (`\label`/`\ref`), citação (`\cite`), figura e tabela;
  - lista.
- **Destaque de sintaxe:** comandos em laranja, ambientes em verde-azulado, matemática em verde e argumentos em claro.
- **Sincronia (SyncTeX):** a linha do cursor no `.tex` aparece realçada no trecho correspondente do PDF, e clicar no PDF leva à linha certa.
- **Proposta:** um comando `/` no editor que insere LaTeX padrão (`\item`, `\cite`, `\href`) ou abre a busca.

## Compilação

- **Quase em tempo real:** compila cerca de 1 segundo depois que você para de digitar, ou ao salvar. Uma nota pequena leva de 1 a 2 segundos.
- **O PDF nunca some.** Enquanto compila, aparece a última versão boa, e o estado muda de "Compilado · há 2 s" para "Compilando…".
- **Um erro não apaga nada.** O PDF continua na versão anterior, e um aviso aponta a linha com problema.
- **Fórmulas instantâneas (proposta):** com o cursor dentro de uma fórmula, um balão mostra a fórmula renderizada a cada tecla.
- **Cada card é um `.tex` pequeno,** e não um documento gigante, o que mantém a compilação rápida.
- **Compor um documento:** selecionar um caminho de notas no grafo e gerar um `.tex` maior (um resumo para a prova, um relatório, o TCC), feito com `\input` das notas, que é LaTeX padrão.

# LaTeX

## O preâmbulo é da trilha

- **O editor mostra a nota a partir de `\begin{document}`.** `\begin{document}`, `\maketitle`, o texto e `\end{document}` ficam na nota e aparecem no editor. O que vem antes (o preâmbulo da trilha, `\title` e `\author`) fica escondido.
- **Cada trilha tem um preâmbulo,** editado numa tela própria da trilha e guardado no arquivo da trilha (`preamble` em `.secondbrain/tracks/<id>.yaml`). O padrão é `\documentclass{article}`, `amsmath` e `hyperref`.
- **O preâmbulo não inclui `\begin{document}`.** Ele é só o que vem antes do documento: `\documentclass`, `\usepackage` e comandos.
- **O arquivo no disco continua completo.** Ao salvar, o app grava o preâmbulo da trilha, o `\title` com o título do card e o `\author`, seguidos da nota como está no editor. Assim o `.tex` compila fora do app, em qualquer distribuição LaTeX.
- **Ao abrir uma nota,** o app separa o que vem antes de `\begin{document}` e descarta esse cabeçalho antigo. Na próxima gravação o cabeçalho é refeito com o preâmbulo atual da trilha, então um cabeçalho escrito à mão no arquivo não sobrevive.
- **Mudar o preâmbulo** vale para cada nota da trilha na próxima vez que ela for gravada.
- **A nota pode sobrescrever o que é global.** Um `\title`, `\author` ou `\date` escrito em qualquer lugar da nota, mesmo depois do `\maketitle`, vale no lugar do título gerado do card e do que vier do preâmbulo da trilha. Linhas comentadas com `%` não contam. Apagar o comando volta ao global, ou seja, ao título do card (D-81).

## Só LaTeX padrão

O `.tex` gravado precisa compilar **fora do programa**, em qualquer distribuição LaTeX, **sem pacote ou classe próprios**.

- **Não usar comandos inventados**, como `\nota{}`, `\tarefa{}`, `\card{}`, `\livro{}`, `\evento{}` ou `\usepackage{trilhas}`.
- **Usar o que é padrão:**
  - `\section` para a estrutura;
  - `itemize` e `enumerate` para listas;
  - `\cite` e `\nocite` com um `.bib` ao lado;
  - `\href` do `hyperref` para ligar notas, de preferência apontando para o PDF da outra nota (`\href{regra-da-cadeia.pdf}{Regra da cadeia}`), o que funciona como link clicável mesmo fora do app;
  - comentários `%`.

O arquivo de uma nota no disco:

```latex
\documentclass{article}      % preâmbulo da trilha
\usepackage{amsmath}
\usepackage{hyperref}

\title{Cálculo II}           % gerado do título do card
\author{}

\begin{document}            % daqui em diante, a nota que o editor mostra
\maketitle

\section{Limites e continuidade}
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
- **Aba do papel aberto:** **>** recolhe o papel de volta para espiando. Não há página dupla (D-77).
- **Zoom do papel:** − e + na barra do papel, ou Ctrl e a roda do mouse. Vai de 25% a 300% e abre em 70%.
- **Salvar PDF:** um botão no papel grava uma cópia do PDF compilado onde o usuário escolher. O PDF de trabalho continua no cache de `.secondbrain/` (D-53).
- **No modal do card,** o estado **Nota** mostra a folha em tela cheia.

## Edição

- **Modo edição:** a nota à esquerda, de `\begin{document}` a `\end{document}`, e o PDF à direita.
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

- **O texto se grava sozinho** cerca de 1 segundo depois que você para de digitar.
- **Compilar é só quando o usuário pede:** **Ctrl S** ou o botão **Compilar** na barra do editor. Digitar não compila (D-80). Uma nota pequena leva de 1 a 2 segundos.
- **Ao abrir uma nota,** o papel mostra o último PDF compilado.
- **O PDF nunca some.** Enquanto compila, aparece a última versão boa, e o estado muda de "Compilado · há 2 s" para "Compilando…".
- **Um erro não apaga nada.** O PDF continua na versão anterior, e um aviso aponta a linha com problema.
- **Fórmulas instantâneas (proposta):** com o cursor dentro de uma fórmula, um balão mostra a fórmula renderizada a cada tecla.
- **Cada card é um `.tex` pequeno,** e não um documento gigante, o que mantém a compilação rápida.
- **Compor um documento:** selecionar um caminho de notas no grafo e gerar um `.tex` maior (um resumo para a prova, um relatório, o TCC), feito com `\input` das notas, que é LaTeX padrão.

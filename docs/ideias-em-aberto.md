# Ideias em aberto

Perguntas sem resposta e ideias que ainda precisam ser avaliadas.

## Dados da nota

- **Onde moram o checklist, o vínculo, o estado e a posição no grafo:** convenção com LaTeX padrão dentro do `.tex`, arquivo de ficha ao lado, ou comentários `%` no topo. Ver [cards e notas](cards-e-notas.md).
- **De onde vem o checklist ao criar a nota:**
  - vazio;
  - modelo por tipo (disciplina traz listas e provas, livro traz capítulos);
  - dos assuntos internos (cada card interno vira "Estudar X");
  - do sumário do livro escolhido;
  - copiado de uma nota parecida.
- **Como definir a bibliografia:**
  - busca por título, autor, ISBN ou DOI;
  - soltar o PDF do livro;
  - biblioteca única por trilha;
  - principal (★) e complementar, com o `\cite` no texto entrando como complementar.
- **De onde vêm os assuntos internos:** manual; as `\section` do `.tex` viram cards internos; da ementa; do sumário de um livro; ou de nomes citados em várias notas.

## Progresso e conclusão

- **Como medir o progresso:**
  - to-dos concluídos;
  - pelos assuntos internos (recursivo, com peso);
  - tempo de estudo em blocos;
  - evidências (exercícios, flashcards, provas);
  - domínio declarado de 0 a 4;
  - uma combinação com pesos por tipo de card.
- **Quando uma nota está concluída:** automático aos 100%, por um marco final (prova ou entrega), por decisão sua (sugerida a partir de 90%) ou com volta periódica para "revisar".
- **O quinto estado, "revisar",** ainda não tem cor nem desenho.

## Grafo

- **A dependência média** é por card específico ou por quantidade de cards concluídos numa região?
- **Como a auto-organização convive** com os cards posicionados à mão?
- **A troca entre os níveis de zoom** é por limites fixos ou com transição suave?
- **No zoom afastado,** as outras camadas aparecem apagadas ao fundo?
- **Títulos longos** ganham um apelido curto para os níveis afastados?
- **No hover das bolinhas,** as não ligadas ficam apagadas, como no Obsidian?
- **Os clusters** ganham um nome escrito fraco ao fundo? As pastas da árvore são os mesmos clusters?
- **Clicar na árvore** faz a câmera voar até o card?
- **Minimapa** no canto, para a visão completa?
- **As conexões médias** aparecem em algum momento, como ao selecionar o card?

## Nota e edição

- **Na página dupla,** a navegação é por rolagem ou por virada de duas em duas páginas?
- **Com o papel aberto,** clicar em outro card troca a nota direto?
- **Na interface única,** as colunas podem ser redimensionadas ou recolhidas?
- **Ao abrir o papel,** o grafo desliza para centralizar o card selecionado?
- **No card novo,** a escolha do tipo muda as seções (Pontual sem tarefas, Projeto com o repositório)?
- **As mudanças do editor do card** salvam na hora ou só ao clicar em Pronto?

## Ideias inspiradas no Obsidian

- **Backlinks** ("Citado em") e **menções não ligadas**.
- **Prévia ao passar o mouse** sobre um link ou card.
- **Grafo local:** só os vizinhos até 2 passos.
- **Apelidos (aliases)** para a mesma nota.
- **Troca rápida (Ctrl O)** e **paleta de comandos (Ctrl P)**.
- **Busca em todo o texto,** incluindo `.tex`, PDFs e a bibliografia.
- **Favoritos e workspaces:** posições de câmera salvas no espaço.
- **Visões salvas e filtros,** como "disciplinas disponíveis com prova este mês".
- **Revisão espaçada e flashcards** a partir das notas.
- **Anotação de PDF:** grifos que viram links na nota.
- **Desenho à mão livre** dentro de uma nota.
- **Versionamento do acervo com git.**

## Ideias inspiradas no Zettelkasten

- **Notas atômicas:** uma ideia por nota, com **vários pais** (por exemplo, "Regra da cadeia" em Cálculo II e em IA I). No grafo de bolinhas, essas notas viram pontes entre clusters.
- **Três tipos de nota:** rascunho (numa caixa de entrada), literatura (ligada à fonte e à página) e permanente.
- **Links com motivo:** "exemplo de", "generaliza", "contradiz", "usa".
- **Continuar e desviar uma ideia** (o Folgezettel), com IDs estáveis.
- **Saúde do acervo:** notas órfãs, notas grandes demais para dividir e nota aleatória ou esquecida.
- **Compor um documento** a partir de um caminho de notas.

## Nota fantasma e calendário

- **Nota fantasma:** citada ou planejada e ainda não criada, com contorno tracejado. Um clique materializa a nota.
- **Calendário:** visão de mês e semana, com provas, entregas, blocos de estudo e revisões, cada evento ligado a um card ou a um trecho da nota.
- **Vincular algo da nota a um evento:** uma data num to-do, arrastar um card para um dia do calendário, e o card mostrando os próximos eventos.
- **Em aberto:** como marcar um evento dentro do `.tex` usando só LaTeX padrão, ou se a data fica fora do `.tex`.

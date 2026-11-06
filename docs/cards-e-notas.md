# Cards e notas

## Tipos de card

| Tipo | Progresso | Exemplo |
|---|---|---|
| Disciplina | Sim, ligado a uma disciplina da graduação | Cálculo II, ligado a 04/2 |
| Estudo independente | Sim, próprio | Um curso de Rust |
| Estudo pontual | Não | Um artigo lido, com as suas notas |
| Projeto | Opcional | Um repositório git |

## Estrutura do card

Inspirada nas caixas de um diagrama de classes, com compartimentos separados por linhas:

1. **Faixa de estado** no topo (azul, amarelo, vermelho ou verde).
2. **Cabeçalho:** o título, mais os botões **maximizar** (abre a trilha interna) e **expandir** (mostra informações extras).
3. **Tarefas:** o checklist. As concluídas ficam marcadas e riscadas.
4. **Rodapé:** barra de progresso com a contagem (5/9) e o vínculo (ícone de disciplina e código 04/2).

**Ao expandir,** aparecem entre as tarefas e o rodapé:

- **Bibliografia:** os livros escolhidos para a disciplina ou o módulo.
- **Vídeos:** links do YouTube, com miniatura 16:9, título e duração.
- **Relacionados:** o que está ligado ao card mas não faz parte dele, como livros avulsos e repositórios git.

**Visual:** cards opacos (nunca translúcidos), uma sombra branca bem leve para contrastar com o fundo quadriculado, e a borda azul quando o card está selecionado.

## Trilha interna (card maximizado)

- O botão maximizar abre um **modal** com um espaço quadriculado próprio.
- **Os cards internos são exatamente iguais aos de fora.** É uma trilha dentro da trilha. Em Cálculo II: Funções de várias variáveis → Limites → Derivadas parciais → (Gradiente, Máximos e mínimos → Lagrange, Integrais duplas → Integrais triplas e Coordenadas polares).
- Os cards internos que não têm trilha própria não mostram o botão maximizar. No componente, isso é a propriedade **Maximizar**.
- O espaço interno também aceita **notas** e **arquivos**, ligados aos cards por conexões fracas.
- Um menu **Exibição** no topo tem interruptores para links fracos, dependências, notas, arquivos, concluídos e bloqueados, mais a ação **Auto-organizar**.

## Nota principal e notas internas

- O modal alterna entre **Nota** e **Trilha**.
- **Cálculo II** tem uma **nota global principal**.
- **Clicar num card interno** abre a nota dele. Uma **seta de voltar** leva à nota principal, e o caminho completo aparece no topo (Engenharia de Computação › Cálculo II › Derivadas parciais).
- O progresso mostrado no topo é o do card aberto.

## Arquivos

- O PDF é um **card comprido**, com uma prévia 9:16 da página, o nome e um botão de expandir.
- **Expandido**, mostra tipo, tamanho, páginas, data em que foi adicionado e pasta.

## Satélites e fantasmas

- **Satélites:** cards pequenos e pontuais (livro avulso, nota, PDF) que só aparecem no zoom próximo, ligados por conexões fracas.
- **Card fantasma:** contorno tracejado, só com o título. Nasce de um link para uma nota que não existe ou de um card recém-criado sem conteúdo. Funciona como roadmap leve. Um clique materializa a nota.

## Editor do card

A solução para editar as informações da nota que não são texto.

- **Abre no lugar do card**, com um clique duplo num card existente ou ao criar um card novo. O espaço desliza, e o painel ocupa o lugar do card.
- **Conteúdo do painel:**
  - nome editável;
  - tipo (Disciplina, Estudo, Pontual ou Projeto) e estado;
  - disciplina vinculada (por exemplo, "04/2 · Cálculo com Funções de Várias Variáveis I"). O **título do card** é curto (Cálculo II) e independe do nome oficial;
  - **tarefas**, com alça para reordenar, caixa para marcar, edição no lugar, data opcional, remover e "+ Adicionar tarefa";
  - **bibliografia**, com a ★ para principal ou complementar, busca por título, autor, ISBN ou DOI, e resultados que mostram o que já está na lista;
  - **vídeos** (colar o link do YouTube) e **relacionados** (vincular nota, livro ou repositório);
  - rodapé com **Excluir card** (ou Descartar, no card novo) e **Pronto**.
- **No card novo:** o título vem focado, a escolha do tipo aparece em destaque e as seções vêm vazias.
- **Com o PDF:** o PDF da nota aparece à direita, com abas para editar o LaTeX, abrir em página dupla ou recolher.
- **Interface única:** editor do card à esquerda, editor `.tex` no centro e PDF à direita, tudo na mesma tela.

## Onde as informações moram (em aberto)

Como o `.tex` só pode ter LaTeX padrão (ver [LaTeX](latex.md)), as opções são:

1. **Convenção com LaTeX padrão no próprio `.tex`:** o checklist como `itemize` numa seção Tarefas, e a bibliografia vinda do `\cite`/`\nocite` com um `.bib` ao lado. Aparece no PDF.
2. **Arquivo de ficha ao lado** do `.tex`, editado só pela interface.
3. **Comentários `%` no topo do `.tex`:** invisíveis no PDF e ignorados por qualquer compilador.

A bibliografia pode ser uma **biblioteca única por trilha** (um livro usado em várias disciplinas existe uma vez só), com a escolha principal ou complementar feita por nota.

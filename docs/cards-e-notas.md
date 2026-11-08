# Cards e notas

## Tipos de card

Um tipo é um **preset inicial**, criado pelo usuário, com:

- um **nome**, o rótulo do tipo;
- um **ícone**, que aparece no rodapé dos cards desse tipo;
- um **conjunto de propriedades** que o card recebe ao ser criado.

O nome e o ícone são compartilhados pelos cards do tipo. As propriedades, não: depois de criado, cada card tem as suas.

- **Qualquer card** pode ganhar propriedades novas a qualquer momento.
- **Trocar o tipo** muda só o nome e o ícone. As propriedades do card continuam as mesmas.
- **Editar um tipo** vale só para os cards criados depois. Os que já existem não mudam.

Exemplos:

| Tipo       | Ícone               | Propriedades                                               |
| ---------- | ------------------- | ---------------------------------------------------------- |
| Disciplina | Chapéu de formatura | Período, bibliografia, vídeos, tarefas, barra de progresso |
| Curso      | Play                | Plataforma, vídeos, tarefas, barra de progresso            |
| Artigo     | Folha               | Autores, DOI, PDF                                          |
| Projeto    | Ramificação do git  | Repositório, tarefas                                       |

## Estrutura do card

Inspirada nas caixas de um diagrama de classes, com compartimentos separados por linhas:

1. **Faixa de estado** no topo (azul, amarelo, vermelho ou verde).
2. **Cabeçalho:** o título, mais os botões **maximizar** (abre o grafo interno) e **expandir** (mostra todas as propriedades).
3. **Tarefas:** o checklist, se o card tiver essa propriedade. As concluídas ficam marcadas e riscadas.
4. **Rodapé:** a barra de progresso com a contagem (5/9), se o card tiver essa propriedade, e o ícone do tipo.

**Progresso:** soma as tarefas concluídas e os cards concluídos do grafo interno. Ao chegar a 100%, o card fica **Concluído** sozinho.

**Ao expandir,** todas as propriedades do card aparecem entre as tarefas e o rodapé. Por exemplo:

- **Bibliografia:** os livros escolhidos para a disciplina ou o módulo.
- **Vídeos:** links do YouTube, com miniatura 16:9, título e duração.
- **Relacionados:** o que está ligado ao card mas não faz parte dele, como livros avulsos e repositórios git.

**Visual:** cards opacos, uma sombra branca bem leve para contrastar com o fundo quadriculado, e a borda quando o card está selecionado.

## Grafo interno (card maximizado)

- O botão maximizar **troca a tela inteira** pelo grafo interno, com o caminho no topo. Não é um modal.
- **Os cards internos são exatamente iguais aos de fora.** É um grafo dentro do card. Em Cálculo II: Funções de várias variáveis → Limites → Derivadas parciais → (Gradiente, Máximos e mínimos → Lagrange, Integrais duplas → Integrais triplas e Coordenadas polares).
- Os cards internos que não têm grafo próprio não mostram o botão maximizar.
- O espaço interno também aceita **notas** e **arquivos**, ligados aos cards por links.
- Um menu **Exibição** no topo tem interruptores para links, conexões, notas, arquivos, concluídos e bloqueados, e os concluídos normais, apagados ou ocultos. A posição dos cards vem sempre da disposição.

## Nota principal e notas internas

- A tela do card alterna entre **Nota** e **Grafo**.
- **Cálculo II** tem uma **nota global principal**.
- **Clicar num card interno** abre a nota dele. Uma **seta de voltar** leva à nota principal, e o caminho completo aparece no topo (Engenharia de Computação › Cálculo II › Derivadas parciais).
- O progresso mostrado no topo é o do card aberto.

## Arquivos

Cada tipo de arquivo tem a sua forma de exibição no grafo. Por exemplo:

- **PDF:** um **card comprido**, com uma prévia 9:16 da página, o nome e um botão de expandir. **Expandido**, mostra tipo, tamanho, páginas, data em que foi adicionado e pasta.
- **EPUB:** o mesmo card comprido, com a capa no lugar da página. Abre no leitor do app, com o texto refluindo na largura, o sumário e a posição de leitura guardada.

## Notas pontuais e fantasmas

- **Notas pontuais:** cards sem propriedade especial, só texto ou uma anotação. Por terem peso baixo, só aparecem no zoom próximo, perto do card ligado a elas que está em foco.
- **Notas fantasmas:** sugestões de novos cards, translúcidas e com contorno tracejado, só com o título. Aparecem ao lado do card quando a nota ganha uma divisão: `\section`, `\chapter` ou um título em Markdown. O botão tracejado **Criar card** transforma a fantasma num card do grafo interno, com uma nota própria para aprofundar aquela divisão; o **×** no topo recusa. A divisão continua na nota principal.

## Editor do card

A solução para editar as informações da nota que não são texto.

- **Abre no lugar do card**, sobre o grafo escurecido, com um clique duplo num card existente ou ao criar um card novo. **Pronto** ou Esc fecha.
- **Conteúdo do painel:**
  - nome editável;
  - tipo e estado;
  - as propriedades do card, com a opção de adicionar novas. O **título do card** pode ser curto (Cálculo II), e o nome completo, se houver, fica numa propriedade;
  - **datas**, cada uma com nome, dia e hora opcional, e "+ Adicionar data";
  - **tarefas**, com alça para reordenar, caixa para marcar, edição no lugar, remover e "+ Adicionar tarefa";
  - **bibliografia**, com a ★ para principal ou complementar, busca por título, autor, ISBN ou DOI, e resultados que mostram o que já está na lista;
  - **vídeos** (colar o link do YouTube) e **relacionados** (vincular nota, livro ou repositório);
  - rodapé com **Excluir card** (ou Descartar, no card novo) e **Pronto**.
- **No card novo:** o título vem focado, a escolha do tipo aparece em destaque e as seções vêm vazias.
- **Com o PDF:** o PDF da nota aparece à direita, com abas para editar o LaTeX ou recolher.
- **Nota Markdown:** o editor `.md` à esquerda e o papel à direita, feito de HTML no WebView e atualizado a cada pausa, sem compilação. Clique duplo no papel leva o cursor ao trecho correspondente.
- **Interface única:** editor do card à esquerda, editor `.tex` no centro e PDF à direita, tudo na mesma tela.

## Bibliografia

Cada trilha tem uma **biblioteca única**: um livro usado em vários cards existe uma vez só. Cada card escolhe da biblioteca os seus livros, marcando-os como principais ou complementares.

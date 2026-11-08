# Interface

O que já está desenhado no Figma, seção por seção.

## Diretrizes visuais

- **Tema escuro**, com as cores e os estilos de texto do arquivo (Inter na interface, JetBrains Mono no código-fonte das notas).
- **Fundo do espaço:** liso, pautado, pontilhado ou quadriculado, à escolha. Os três últimos usam uma grade dinâmica de dois níveis: ao aproximar, quadrinhos 5 vezes menores surgem aos poucos; ao afastar, os menores somem e os maiores assumem.
- **Cards opacos,** com uma sombra branca bem leve. Nada translúcido.
- **Estado só pela cor da faixa,** sem rótulo de texto.

## Componentes

- **Card de trilha:** variantes recolhido e expandido, mais a propriedade Maximizar.
- **Card de trilha — níveis de zoom:** variantes Muito longe, Longe e Médio.
- **Nota, Livro avulso e Arquivo:** o arquivo tem as variantes recolhido e expandido, com a prévia 9:16.
- **Interruptor:** ligado e desligado.

## Card maximizado

1. **Grafo interno:** modal com o grafo de Cálculo II, notas e arquivos. Botão Exibição e menu de interruptores.
2. **Nota principal:** a folha de Cálculo II.
3. **Nota de um card interno:** Derivadas parciais, com a seta de voltar e o caminho completo.
4. **Edição LaTeX:** o `.tex` e o PDF lado a lado.
5. **Nota espiando:** a tira do papel na borda direita, com a aba **<**.
6. **Nota ao lado:** o papel aberto sobre o grafo, com as abas **<** e **>**.
7. **Página dupla:** duas páginas lado a lado, com a aba **>**.

## Níveis de zoom

Do mais longe para o mais perto:

1. **Mais afastado:** bolinhas na cor do estado, próximas pelo quanto se referenciam, com tamanho pelo peso e hover com o nome.
2. **Muito afastado:** miniaturas sem texto, só a faixa do estado e o progresso, com as conexões.
3. **Afastado:** miniaturas maiores, com hover mostrando o título e o progresso.
4. **Médio:** o card menor, com título e progresso legíveis.
5. **Próximo:** o card completo, com as notas pontuais e os links.

## Tela inicial

Ao abrir o app (depois da primeira vez), aparece uma tela no estilo da primeira abertura com duas listas: os **cards modificados recentemente** e os **cards com Data perto de hoje**, de todas as trilhas. Clicar num card abre a trilha dele com a câmera no card. Regras em [modelagem.md, 5.1](modelagem.md).

## Explorador e nova trilha

- **Explorador em árvore à esquerda,** colapsável. Mostra trilhas, pastas, cards (com a cor do estado), notas `.tex` e arquivos. Tem busca (Ctrl Space) e o botão **+**, que cria um card novo na mesma pasta do card selecionado. Arquivo guardado só pelo caminho mostra ↗; caminho quebrado aparece riscado em vermelho com ⚠.
- **Explorador recolhido:** uma barra de ícones de 52 px.
- **Nova trilha sem diálogo:** o botão cria a trilha na hora, como uma linha nova na árvore com o campo de nome aberto, e o espaço vazio da trilha abre ao lado.

## Editor do card

1. **Clique duplo num card existente:** o painel estendido.
2. **Card novo:** o painel com o título focado e a escolha do tipo.
3. **Editor no lugar do card, com o PDF à direita:** o espaço desliza para a esquerda.
4. **Interface única** (1920×1080): editor do card, `.tex` e PDF.

## Telas v2

A página **Telas v2** do Figma é a versão atual. Cada interação dela, com o que faz nos dados, está em [modelagem.md, seção 5](modelagem.md). As seções:

- **Componentes v2:** ícones, botão, chip de opção, barra da trilha (uma variante por visualização), explorador, cartão, nota fantasma e elementos (imagem, repositório, arquivo só pelo caminho, arquivo não encontrado).
- **Visualizações:** kanban agrupado por estado e por outra seleção, lista, grafo com filtros, calendário em mês e em semana.
- **Grafo:** criar conexão arrastando, notas fantasmas, zoom próximo com elementos, adicionar arquivo, link automático, buscar arquivo e soltar num card.
- **Notas, bibliografia e LaTeX:** compilando, erro, biblioteca e compor documento.
- **App:** primeira abertura, troca rápida (Ctrl Space), paleta de comandos (Ctrl P) e as configurações.
- **Explorador recolhido, card expandido, níveis de zoom, disposição radial, prévia do PDF e edição do card.**

## Quadro de ideias

**Ideias — checklist, bibliografia e informações da nota:**

- **A.** Onde as informações moram.
- **B.** Onde aparecem e como se editam.
- **C.** De onde vem o checklist ao criar a nota.
- **D.** Como definir a bibliografia.

Ver [ideias em aberto](ideias-em-aberto.md).

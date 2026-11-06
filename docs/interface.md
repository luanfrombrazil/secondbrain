# Interface

O que já está desenhado na página **Trilhas** do Figma, seção por seção.

## Diretrizes visuais

- **Tema escuro**, com as cores e os estilos de texto do arquivo (Inter na interface, JetBrains Mono no código e nos códigos de disciplina).
- **Fundo do espaço:** um plano quadriculado, com uma grade fina de 24 px e linhas mais marcadas a cada 120 px.
- **Cards opacos,** com uma sombra branca bem leve. Nada translúcido.
- **Estado só pela cor da faixa,** sem rótulos como "Estudando".
- **Sem frases explicativas nas telas.**

## Componentes

- **Card de trilha:** variantes recolhido e expandido, mais a propriedade Maximizar.
- **Card de trilha — níveis de zoom:** variantes Muito longe, Longe e Médio.
- **Nota, Livro avulso e Arquivo:** o arquivo tem as variantes recolhido e expandido, com a prévia 9:16.
- **Interruptor:** ligado e desligado.

## Card maximizado

1. **Trilha interna:** modal com o grafo de Cálculo II, notas e arquivos. Botão Exibição e menu de interruptores.
2. **Nota principal:** a folha de Cálculo II.
3. **Nota de um card interno:** Derivadas parciais, com a seta de voltar e o caminho completo.
4. **Edição LaTeX:** o `.tex` e o PDF lado a lado.
5. **Nota espiando:** a tira do papel na borda direita, com a aba **<**.
6. **Nota ao lado:** o papel aberto sobre o grafo, com as abas **<** e **>**.
7. **Página dupla:** duas páginas lado a lado, com a aba **>**.

## Níveis de zoom

1. **Muito afastado, em bolinhas:** a escolhida. Clusters por área, tamanho pelo conteúdo e hover com o nome.
2. **Muito afastado, em códigos:** a alternativa descartada, com cards mostrando só o código e a faixa.
3. **Afastado:** faixa, código e título grande.
4. **Médio:** mais o progresso.
5. **Próximo:** o card completo, com os satélites e os links fracos.

## Explorador e nova trilha

- **Explorador em árvore à esquerda,** colapsável. Mostra trilhas, áreas (pastas), cards (com a cor do estado), notas `.tex` e arquivos. Tem busca (Ctrl O) e uma alternância árvore/grafo.
- **Explorador recolhido:** uma barra de ícones de 52 px.
- **Nova trilha sem diálogo:** o botão cria a trilha na hora, como uma linha nova na árvore com o campo de nome aberto, e o espaço vazio da trilha abre ao lado.

## Editor do card

1. **Clique duplo num card existente:** o painel estendido.
2. **Card novo:** o painel com o título focado e a escolha do tipo.
3. **Editor no lugar do card, com o PDF à direita:** o espaço desliza para a esquerda.
4. **Interface única** (1920×1080): editor do card, `.tex` e PDF.

## Quadro de ideias

**Ideias — checklist, bibliografia e informações da nota:**

- **A.** Onde as informações moram.
- **B.** Onde aparecem e como se editam.
- **C.** De onde vem o checklist ao criar a nota.
- **D.** Como definir a bibliografia.

Ver [ideias em aberto](ideias-em-aberto.md).

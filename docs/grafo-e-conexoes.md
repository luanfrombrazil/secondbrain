# Grafo e conexões

## Camadas

- Cada **trilha** é uma camada independente: "Engenharia de Computação", "Idiomas", "Leituras".
- A ideia original é uma visualização **3D**, com as camadas empilhadas na vertical e os cards espalhados em todas as direções.
- **Primeira versão:** cada camada é uma **tela plana infinita**, com arrastar e zoom, e você troca de camada como quem troca de andar. O 3D de verdade fica para depois.
- **Conexões entre trilhas** são permitidas. Por exemplo, a hierarquia de Chomsky (computação) pode ter um link com as línguas (idiomas).

## Posição

| Eixo | Significado |
|---|---|
| Horizontal | Profundidade de pré-requisitos. Quanto mais à direita, maior a cadeia de dependências antes do card. |
| Vertical | Área, por exemplo programação no topo, matemática no meio, hardware e redes embaixo. |

**Os vazios são esperas.** Um card que junta pré-requisitos de ramos diferentes fica empurrado para a direita, depois de todos eles. É o caso de Ciência de Dados e Compiladores na grade de Engenharia de Computação, que esperam Linguagens Formais, Algoritmos e Estruturas de Dados e o Cálculo.

A inspiração é a **árvore de habilidades de jogos**: alocar pontos nas habilidades anteriores libera as próximas, e a posição já mostra o que vem antes.

## Três forças de conexão

| Conexão | Como aparece | Para que serve |
|---|---|---|
| **Forte** | Linha contínua com seta | Caminho direto de progresso. Cálculo I → Cálculo II → Cálculo III → EDO → EDP. |
| **Média** | Nenhuma linha. Só a posição, porque o card fica à direita de tudo de que depende | Dependência real que libera o estudo, sem poluir o desenho. Por exemplo, Ciência de Dados depende de Algoritmos e Estruturas de Dados II. |
| **Fraca** | Linha tracejada, só com zoom próximo | Um link entre notas. Por exemplo, Estatística e o livro *O Andar do Bêbado*. |

- Uma conexão forte pode **cruzar ramos**. Sistemas de Controle depende muito de EDO, mesmo EDO estando em outro ramo.
- **A dependência libera o estudo.** Um card só fica "disponível" quando o que ele exige está concluído.
- **Em aberto:** a dependência média pode ser por card específico ("depois de AED II") ou por quantidade ("depois de concluir N cards desta região"), como os níveis de uma árvore de habilidades.

## Edição no espaço

- Criar uma nota em qualquer ponto do espaço.
- Arrastar de um card para outro para criar uma conexão, inclusive de dependência.
- Mover os cards livremente.
- **Auto-organização:** coluna pela cadeia de dependências mais longa, linha pela área. É um layout em camadas (o algoritmo de Sugiyama).
- **Em aberto:** como a auto-organização convive com os cards posicionados à mão (botão, ajuste contínuo ou respeitar os fixados).

## Zoom semântico

O que aparece depende da distância. As posições são as mesmas em todos os níveis, e só muda o quanto cada card mostra.

| Nível | O card mostra | O que mais aparece |
|---|---|---|
| Muito afastado | Uma **bolinha**, como no Obsidian | Clusters por área. O tamanho da bolinha segue a quantidade de conteúdo interno. O hover mostra o nome, o código e a quantidade de itens. |
| Afastado (~42%) | Faixa de estado, código da disciplina e título grande | Só as conexões fortes |
| Médio (~72%) | Mais a barra de progresso e a contagem | Só as conexões fortes |
| Próximo (100%) | O card completo, com tarefas, código e botões | Satélites (livros avulsos, notas, PDFs) e as conexões fracas |

- **Escolha para o muito afastado:** as bolinhas, em vez dos cards só com o código.
- No hover das bolinhas, as conexões da bolinha ficam claras. **Em aberto:** apagar ou não as bolinhas que não estão ligadas à selecionada.
- **Cada card tem um peso** (principal ou satélite), que decide em que nível ele aparece.

## Grade de Engenharia de Computação (exemplo usado nos protótipos)

São 68 disciplinas, com Sistemas Distribuídos aparecendo uma vez só. As áreas usadas nos clusters:

- **Fundamentos:** Introdução à Engenharia de Computação, Filosofia da Tecnologia, Linguagens Formais e Autômatos, Métodos Numéricos Computacionais, Metodologia Científica, Introdução à Economia, Legislação, Compiladores.
- **Programação:** Fundamentos de Programação I e II, POO, Algoritmos e Estruturas de Dados I e II, Banco de Dados I a III, Conceitos de Linguagens, Modelagem, Desenvolvimento de Sistemas, Engenharia de Software I e II, Usabilidade, Gestão de Projeto, Programação Concorrente, Programação para GPU, Gestão de Produtos e Serviços.
- **Matemática e física:** Cálculo (uma variável, várias variáveis I e II, variável complexa), Geometria Analítica, Integração e Séries, Álgebra Linear, EDO, EDP, Estatística, Mecânica, OFT, Eletromagnetismo, Física Moderna.
- **IA e dados:** Ciência de Dados I e II, IA I e II, Otimização I, Inteligência Computacional, Computação Evolucionária, Métodos Numéricos Avançados, Reconhecimento de Padrões, Inteligência Computacional para Otimização, Sistemas Bio-inspirados, Otimização Combinatória.
- **Hardware:** Circuitos Elétricos, Matemática Discreta, Sistemas Digitais, Arquitetura e Organização de Computadores I a III, Sistemas Operacionais, Sistemas Embarcados (laboratório I e II), Controle de Sistemas Dinâmicos e Controle Digital.
- **Redes:** Redes de Computadores I a IV, Sistemas Distribuídos.

As conexões fortes desenhadas pelo usuário vão de Introdução para Filosofia da Tecnologia, Circuitos e Redes I. De Filosofia, seguem para Linguagens Formais, Métodos Numéricos, Metodologia e Economia (que leva a Legislação), convergindo em Ciência de Dados I e Compiladores, e daí para IA I e o ramo de IA. Redes segue em linha de I a IV e termina em Sistemas Distribuídos. As outras conexões dentro de cada ramo foram inferidas nos protótipos e precisam ser conferidas.

# Grafo e conexões

## Trilhas

- Cada **trilha** é um grafo independente de cards: "Engenharia de Computação", "Idiomas", "Leituras".
- Cada trilha é uma **tela plana infinita**, com arrastar e zoom.
- As trilhas são **independentes**: a ordem e as dependências de uma trilha não dependem de nenhuma outra.

## Conexões

| Tipo | Como aparece | Para que serve |
|---|---|---|
| **Conexão** | Linha contínua com seta | Ordem, precedência e dependência. Cálculo I → Cálculo II → Cálculo III → EDO → EDP. |
| **Link** | Linha tracejada, só com zoom próximo | Um link entre notas e elementos do grafo, para navegar entre eles. Por exemplo, Estatística e o livro *O Andar do Bêbado*. |

- Uma conexão pode **cruzar ramos**. Sistemas de Controle depende muito de EDO, mesmo EDO estando em outro ramo.
- **A dependência libera o card.** Um card só fica disponível quando o que ele exige está concluído.

## Disposição

O grafo pode ser arrumado de várias formas. O usuário não move os cards: escolhe a disposição, e a posição é calculada. A exceção é o card solto, ver "Edição no espaço".

- **Por dependência (horizontal):** cada card fica à direita de tudo de que depende, e a ordem dentro de cada coluna diminui os cruzamentos. É um layout em camadas, no estilo do algoritmo de Sugiyama.
- **Por forças:** os cards se repelem e cada conexão ou link puxa os dois cards um para o outro. Notas que se referenciam muito ficam juntas, e os clusters aparecem sozinhos. A simulação assenta sozinha; o usuário não arrasta cards.
- **Árvore circular (radial):** cada filho fica em volta do próprio pai, em anéis por nível de dependência. Todas as setas têm o mesmo comprimento e nenhuma atravessa um card; um card com dois pais fica no ângulo entre eles.

Quando um card se ramifica em vários filhos, todas as setas curvam e o pai fica centrado entre eles.

Escolher uma disposição faz os cards deslizarem até o lugar novo, e a câmera centraliza o grafo.

Na disposição por dependência, **os vazios são esperas.** Um card que junta pré-requisitos de ramos diferentes fica empurrado para a direita, depois de todos eles. É o caso de Ciência de Dados e Compiladores na grade de Engenharia de Computação, que esperam Linguagens Formais, Algoritmos e Estruturas de Dados e o Cálculo.

A inspiração é a **árvore de habilidades de jogos**: alocar pontos nas habilidades anteriores libera as próximas, e a posição já mostra o que vem antes.

## Edição no espaço

- Criar um card num ponto do espaço. **Card solto fica onde foi criado** (decidido em 07/11/2026). Criar um card num ponto do espaço é a única vez em que o usuário escolhe uma posição: o card fica **fixo** ali enquanto não tiver nenhuma conexão (dependência). Enquanto está fixo, ele **repele** os cards em volta, que se afastam numa animação que parece magnética, para não ficar nada por baixo dele. Quando ganha a primeira conexão com outro card, perde a posição fixa e passa a ser arrumado pela disposição junto com os outros. Link não solta o card; só conexão.
- Arrastar da alça de um card até outro cria uma **conexão** (dependência), só entre cards da mesma trilha.
- **Links** são automáticos: escrever uma referência na nota (`\href{…}` no `.tex`, `[[…]]` no `.md`) cria o link, e apagar a referência apaga. Soltar um elemento num card cria um link manual, que não aparece no texto.
- Soltar um arquivo de fora no espaço pergunta se ele vai para a pasta ou se o app guarda só o caminho.

Todas as interações das telas, uma a uma, estão em [modelagem.md, seção 5](modelagem.md).

## Zoom semântico

O que aparece depende da distância. As posições são as mesmas em todos os níveis, e só muda o quanto cada card mostra.

| Nível | O card mostra | O que mais aparece |
|---|---|---|
| Mais afastado | Uma **bolinha** na cor do estado | Clusters que se formam sozinhos: quanto mais duas notas se referenciam, mais perto ficam. O tamanho da bolinha segue o peso do card. |
| Muito afastado | Uma **miniatura sem texto**, só a faixa do estado e o progresso | As conexões |
| Afastado (~42%) | Uma **miniatura do próprio card** | Só as conexões |
| Médio (~72%) | O card menor, com título e progresso legíveis | Só as conexões |
| Próximo (100%) | O card completo, com tarefas e botões | Notas pontuais (livros avulsos, artigos, PDFs) e os links |

- **Hover:** nos níveis afastados, passar o mouse mostra o nome do card perto dele, como o nome de um nó no grafo, e destaca as suas conexões.
- **Cada card tem um peso** (principal ou pontual), que decide em que nível ele aparece.

## Grade de Engenharia de Computação (exemplo usado nos protótipos)

São 68 disciplinas, com Sistemas Distribuídos aparecendo uma vez só. Agrupadas por área:

- **Fundamentos:** Introdução à Engenharia de Computação, Filosofia da Tecnologia, Linguagens Formais e Autômatos, Métodos Numéricos Computacionais, Metodologia Científica, Introdução à Economia, Legislação, Compiladores.
- **Programação:** Fundamentos de Programação I e II, POO, Algoritmos e Estruturas de Dados I e II, Banco de Dados I a III, Conceitos de Linguagens, Modelagem, Desenvolvimento de Sistemas, Engenharia de Software I e II, Usabilidade, Gestão de Projeto, Programação Concorrente, Programação para GPU, Gestão de Produtos e Serviços.
- **Matemática e física:** Cálculo (uma variável, várias variáveis I e II, variável complexa), Geometria Analítica, Integração e Séries, Álgebra Linear, EDO, EDP, Estatística, Mecânica, OFT, Eletromagnetismo, Física Moderna.
- **IA e dados:** Ciência de Dados I e II, IA I e II, Otimização I, Inteligência Computacional, Computação Evolucionária, Métodos Numéricos Avançados, Reconhecimento de Padrões, Inteligência Computacional para Otimização, Sistemas Bio-inspirados, Otimização Combinatória.
- **Hardware:** Circuitos Elétricos, Matemática Discreta, Sistemas Digitais, Arquitetura e Organização de Computadores I a III, Sistemas Operacionais, Sistemas Embarcados (laboratório I e II), Controle de Sistemas Dinâmicos e Controle Digital.
- **Redes:** Redes de Computadores I a IV, Sistemas Distribuídos.

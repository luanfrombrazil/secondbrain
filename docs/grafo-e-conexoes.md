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

Na disposição por dependência, quando um card se ramifica em vários filhos, todas as setas curvam e o pai fica centrado entre eles. Um filho com vários pais fica no meio deles, também com setas curvas. Com um filho e um pai, a seta é reta. Nas disposições radial e por forças, todas as conexões são retas.

As **pastas comuns** (sem nota de mesmo nome) não aparecem no grafo: não há contorno, área nem nome de pasta desenhado. O único efeito delas é a **atração da pasta** na disposição por forças, que aproxima os cards da mesma pasta. Nas disposições por dependência e radial, as pastas não mudam a ordem nem a posição dos cards (decidido em 08/11/2026).

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
| Bolinha (abaixo de 14%) | Uma **bolinha** na cor do estado | Clusters que se formam sozinhos: quanto mais duas notas se referenciam, mais perto ficam. O tamanho da bolinha segue o peso do card. |
| Miniatura (14% a 55%) | Uma **miniatura sem texto**, só a faixa do estado e o progresso | As conexões |
| Médio (55% a 86%) | O card menor, com título e progresso legíveis | Só as conexões |
| Próximo (86% ou mais) | O card completo, com tarefas e botões | Notas pontuais (livros avulsos, artigos, PDFs) e os links |

- **Hover:** nos níveis afastados, passar o mouse mostra o nome do card perto dele, como o nome de um nó no grafo, e destaca as suas conexões.
- **Cada card tem um peso** (principal ou pontual), que decide em que nível ele aparece.
- As trocas de nível são ajustáveis (ver "Ajustes do grafo"). Cada troca tem uma margem de 5% para o card não piscar quando o zoom para em cima do limite.
- O zoom vai de **3% a 250%**. Centralizar o grafo nunca passa de 100%.

## Ajustes do grafo

Os números que o laboratório do grafo deixa mexer viram **configurações do usuário**, em **Configurações › Grafo** (ver `interface.md`). Mexer num controle rearruma o grafo na hora, com os cards deslizando até o lugar novo. **Restaurar padrões** volta tudo aos valores abaixo.

Os padrões são os do laboratório hoje e ainda podem mudar até serem aprovados lá. O resfriamento e o raio magnético já foram fixados no laboratório e não são configuráveis (ver "O que não é ajustável"). As distâncias são em pixels com o zoom em 100%; o card mede 220 × 110.

### Setas

| Ajuste | Padrão | Faixa | O que faz |
|---|---|---|---|
| **Espessura das setas** | 2,2 | 0,5 a 8 | A espessura da linha das conexões, em pixels com o zoom em 100%. Vale nas três disposições. Ao afastar, a linha afina junto com o zoom, mas nunca fica abaixo de 1 px; a seta destacada (hover ou seleção) fica um pouco mais grossa. |

### Fundo

O usuário só escolhe o tipo de fundo (liso, pautado, pontilhado ou quadriculado), em Visualização padrão e em cada trilha. A grade tem dois níveis: ao aproximar, quadrinhos 5 vezes menores surgem aos poucos dentro de cada quadrado; ao afastar, os menores somem e os maiores assumem. Os números do desenho foram fixados no laboratório e não são ajustáveis:

| Valor fixo | Valor | O que é |
|---|---|---|
| **Opacidade** | 20% | Quanto as linhas ou os pontos do fundo aparecem. |
| **Tamanho da grade** | 230 | O lado de um quadrado da grade, em pixels com o zoom em 100%. |
| **Menor quadrinho na tela** | 15 | O menor tamanho, em pixels de tela, que um quadrinho pode ter antes de sumir. |
| **Espessura** | 1,2 | A espessura das linhas, ou o tamanho dos pontos no pontilhado. |

### Por dependência

| Ajuste | Padrão | Faixa | O que faz |
|---|---|---|---|
| **Folga entre colunas** | 130 | 30 a 400 | Espaço horizontal entre uma coluna de cards e a próxima. |
| **Folga entre linhas** | 40 | 5 a 200 | Espaço vertical entre os cards de uma mesma coluna. |

### Radial

| Ajuste | Padrão | Faixa | O que faz |
|---|---|---|---|
| **Distância entre anéis** | 400 | 260 a 900 | Comprimento de cada seta, do pai ao anel dos filhos. Como todas as setas têm o mesmo comprimento, é também o raio de um anel para o próximo. |

### Por forças

| Ajuste | Padrão | Faixa | O que faz |
|---|---|---|---|
| **Repulsão** | 14 000 | 1 000 a 80 000 | Quanto os cards se empurram. Mais alto espalha o grafo. |
| **Força da mola** | 0,12 | 0,01 a 0,6 | Quanto cada conexão puxa os dois cards. Um link puxa com 35% dessa força. |
| **Comprimento da mola** | 340 | 100 a 900 | A distância em que uma conexão não puxa nem empurra. |
| **Gravidade** | 0,015 | 0 a 0,12 | Puxa tudo para o centro, para os cards sem conexão não fugirem. |
| **Atrito** | 0,6 | 0,2 a 0,95 | Quanto da velocidade cada card mantém de um quadro para o outro. Mais alto balança mais antes de parar. |
| **Atração da pasta** | 0,03 | 0 a 0,2 | Puxa os cards de uma mesma pasta comum para o centro deles. É o único efeito das pastas no grafo. |

A simulação é determinística: o mesmo grafo com os mesmos ajustes dá sempre o mesmo desenho. Nenhum card fica por cima de outro; sobra sempre uma folga de 16 px.

### Zoom semântico

| Troca | Padrão | Faixa |
|---|---|---|
| **Bolinha → miniatura** | 14% | 5% a 150% |
| **Miniatura → médio** | 55% | 5% a 150% |
| **Médio → próximo** | 86% | 5% a 150% |

Cada valor é o zoom em que o card passa de um nível para o próximo. Os três ficam sempre em ordem crescente. Não há o nível intermediário que simulava o card com texto pequeno: ao aproximar, a miniatura sem texto passa direto para o card médio (decidido em 08/11/2026).

### O que não é ajustável

O **resfriamento** (0,08: quanto a agitação da simulação por forças cai a cada quadro), o **raio magnético** do card solto (200 px de folga em volta dele, de onde os vizinhos são empurrados; ver "Edição no espaço"), os números do **fundo** (ver "Fundo" acima), o zoom mínimo e máximo, a margem de 5% das trocas de nível, a força relativa dos links (35% da mola), a folga de 16 px entre cards e o peso maior do card solto na repulsão (3 vezes) são fixos.

## Grade de Engenharia de Computação (exemplo usado nos protótipos)

São 68 disciplinas, com Sistemas Distribuídos aparecendo uma vez só. Agrupadas por área:

- **Fundamentos:** Introdução à Engenharia de Computação, Filosofia da Tecnologia, Linguagens Formais e Autômatos, Métodos Numéricos Computacionais, Metodologia Científica, Introdução à Economia, Legislação, Compiladores.
- **Programação:** Fundamentos de Programação I e II, POO, Algoritmos e Estruturas de Dados I e II, Banco de Dados I a III, Conceitos de Linguagens, Modelagem, Desenvolvimento de Sistemas, Engenharia de Software I e II, Usabilidade, Gestão de Projeto, Programação Concorrente, Programação para GPU, Gestão de Produtos e Serviços.
- **Matemática e física:** Cálculo (uma variável, várias variáveis I e II, variável complexa), Geometria Analítica, Integração e Séries, Álgebra Linear, EDO, EDP, Estatística, Mecânica, OFT, Eletromagnetismo, Física Moderna.
- **IA e dados:** Ciência de Dados I e II, IA I e II, Otimização I, Inteligência Computacional, Computação Evolucionária, Métodos Numéricos Avançados, Reconhecimento de Padrões, Inteligência Computacional para Otimização, Sistemas Bio-inspirados, Otimização Combinatória.
- **Hardware:** Circuitos Elétricos, Matemática Discreta, Sistemas Digitais, Arquitetura e Organização de Computadores I a III, Sistemas Operacionais, Sistemas Embarcados (laboratório I e II), Controle de Sistemas Dinâmicos e Controle Digital.
- **Redes:** Redes de Computadores I a IV, Sistemas Distribuídos.

# Trilhas

> Nome provisório. Projeto em fase de **planejamento**: ainda não existe código, só ideias e protótipos no Figma.

Trilhas é um aplicativo de notas, diferente do Obsidian, que serve ao mesmo tempo de **second brain** (o que você já estudou) e de **roadmap** (o que vem depois). As notas não ficam numa lista nem numa pasta: ficam num **grafo de cards**, espalhados num espaço infinito, ligados por dependências e por links, e organizados em **camadas** independentes, as trilhas.

Cada card é uma nota em **LaTeX** que compila em qualquer lugar, sem nada próprio do programa. Cada card também pode ter a sua própria **trilha interna**, com cards menores, arquivos e notas.

---

## A ideia em uma imagem

```
Camada "Engenharia de Computação"

  [Cálculo I] ──► [Integração e Séries] ──► [Várias Variáveis I] ──► [Várias Variáveis II] ──► [EDO]
                          │                          │
                          └──► [Estatística]         └──► [Fundamentos de Mecânica] ──► [OFT]
                                    ┊
                                    ┊ (link fraco, só aparece de perto)
                                    ┊
                             (O Andar do Bêbado)

Camada "Idiomas"

  [Línguas] ──► [Francês] ──► [A1] ──► [A2]
            └─► [Inglês]  ──► [A1] ──► [A2]
```

- **Da esquerda para a direita:** quanto mais à direita, mais pré-requisitos o assunto tem.
- **De cima para baixo:** a área (programação, matemática, hardware, redes...).
- **Cor da faixa no topo do card:** o estado do estudo.

---

## Princípios

1. **O grafo é a tela principal.** As notas são cards num espaço infinito, e não linhas numa lista.
2. **Um card é uma nota.** O card do grafo é uma miniatura da nota completa.
3. **Dependência é estrutura.** O que você precisa saber antes define a posição e libera o estudo.
4. **Menos é mais de longe, mais é mais de perto.** O zoom semântico decide o que aparece em cada nível.
5. **Os arquivos são seus.** As notas são `.tex` comuns, que compilam em qualquer distribuição LaTeX, sem pacote ou classe próprios.
6. **Sem textos explicativos na interface.** As telas mostram os dados, e as regras ficam na documentação.

---

## Conceitos

| Termo | O que é |
|---|---|
| **Trilha** | Uma camada independente do grafo, como "Engenharia de Computação", "Idiomas" ou "Leituras". |
| **Card** | Uma nota no grafo. Tem título, estado, tarefas, progresso e, expandido, bibliografia, vídeos e relacionados. |
| **Trilha interna** | O grafo de dentro de um card, como os assuntos de Cálculo II. Usa os mesmos cards, de forma recursiva. |
| **Nota** | O texto do card, um arquivo `.tex` no estilo `article`. |
| **Conexão forte** | Dependência direta. Linha contínua com seta. |
| **Conexão média** | Dependência real que não é desenhada. Aparece só na posição, porque o card fica à direita de tudo de que depende. |
| **Conexão fraca** | Um link entre notas, sem dependência. Linha tracejada, que só aparece de perto. |
| **Satélite** | Card pequeno e pontual (um livro avulso, uma nota solta, um PDF) que só aparece quando você se aproxima do card principal. |
| **Card fantasma** | Uma nota planejada ou citada que ainda não existe. Contorno tracejado. |

---

## Estado do card

O estado aparece numa **faixa colorida no topo do card**, sem rótulo de texto.

| Cor | Estado |
|---|---|
| Azul | Em andamento |
| Amarelo | Disponível para começar |
| Vermelho | Bloqueado, com dependência não cumprida |
| Verde | Estudado |
| *(proposta)* | Revisar: um estudado que pede revisão depois de um tempo |

---

## Documentação

| Documento | Conteúdo |
|---|---|
| [Grafo e conexões](docs/grafo-e-conexoes.md) | Camadas, conexões, dependências, zoom semântico, auto-organização, bolinhas e 3D |
| [Cards e notas](docs/cards-e-notas.md) | Tipos de card, estrutura, trilha interna, arquivos, satélites, fantasmas e o editor do card |
| [LaTeX](docs/latex.md) | Regra do LaTeX padrão, aparência, leitura, edição, sincronia e compilação |
| [Interface](docs/interface.md) | Telas desenhadas no Figma, uma por uma |
| [Decisões](docs/decisoes.md) | O que já foi decidido, com data |
| [Ideias em aberto](docs/ideias-em-aberto.md) | Perguntas sem resposta e ideias para avaliar |
| [Viabilidade técnica](docs/viabilidade.md) | O que é fácil, o que é difícil e as ferramentas que existem |
| [Stack](docs/stack.md) | Java 25, JavaFX e Maven, e como cada peça do app se encaixa |

---

## Figma

Os protótipos estão na página **Trilhas** do arquivo do Figma:
<https://www.figma.com/design/0BqwZD0E88qRrWcxHGRb49/OvertimeDispatcher?node-id=110-4139>

A página está dividida em seções:

1. **Componentes:** card de trilha, nota, livro avulso, arquivo, interruptor e as variantes do card por nível de zoom.
2. **Card maximizado — trilha, nota e edição:** o modal do card nos estados trilha interna, nota principal, nota de um card interno, edição LaTeX, nota espiando, nota ao lado e página dupla.
3. **Níveis de zoom:** de bolinhas e códigos (muito afastado) até o card completo (100%).
4. **Explorador e nova trilha:** a árvore à esquerda, aberta e recolhida, e a criação direta de uma trilha.
5. **Editor do card:** o painel estendido, no lugar do card, com o PDF ao lado, e a interface única com card, LaTeX e PDF.
6. **Ideias — checklist, bibliografia e informações da nota:** quadro de ideias ainda não decidido.

---

## Origem

A ideia nasceu como a seção "Trilhas" do Overtimer, um despachante de agentes. Em 06/11/2026 virou um projeto próprio: **um aplicativo de notas independente**, sem relação com o Overtimer.

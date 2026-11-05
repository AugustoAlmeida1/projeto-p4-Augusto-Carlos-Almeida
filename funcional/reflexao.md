# [P4-ETAPA-06] Reflexão — Implementação Funcional (Haskell)

## 1. Como o raciocínio mudou?

Nas etapas 03 e 04 a pergunta central era **"que estado devo modificar?"**.
Em C, a resposta era `capacidadeUsada += peso`. Em Java, era
`entregador.atribuir(pedido)`, que muda `cargaAtual` e `rota` por dentro do
objeto. Mudou *quem* mexe no estado, mas o raciocínio continuava o mesmo:
existe um entregador que vai sendo alterado ao longo da execução.

Em Haskell a pergunta passou a ser **"que transformação devo aplicar aos
dados?"**. Não existe um entregador sendo alterado. Existe uma lista de
pedidos que passa por uma sequência de transformações, e cada uma devolve um
valor novo:

```
pedidos --partition--> (válidos, inválidos)
válidos --sortBy-----> ordenados
ordenados --distribuir--> (rotas, pendentes)
```

Consequência concreta: não existe `reiniciar()` nem "resetar
capacidadeUsada". Nas versões anteriores era preciso zerar o estado antes de
cada execução. Aqui isso não faz sentido, porque nada foi modificado.
Chamar `alocar` duas vezes com os mesmos argumentos devolve o mesmo
resultado, sempre. Além disso, a carga de uma rota deixou de ser um campo
guardado e passou a ser **calculada** (`cargaRota = sum . map pedPeso . rotaPedidos`),
então não existe risco de ela ficar desatualizada.

## 2. O que identifiquei no código

### Funções puras
Todas as funções de `Alocacao.hs`: `pedidoValido`, `compararPedidos`,
`cargaRota`, `podeReceber`, `escolherRota`, `atribuir`, `substituirRota`,
`distribuir` e `alocar`. Todas dependem só dos argumentos, não leem nada
externo e não alteram nada fora delas. Por isso têm **transparência
referencial**: qualquer chamada pode ser trocada pelo seu resultado sem mudar
o comportamento do programa.

### Funções impuras
Apenas `main`, em `Testes.hs`. É a única que faz entrada/saída (`putStrLn`,
`putStr`, `exitSuccess`/`exitFailure`). O tipo `IO ()` deixa isso explícito:
o compilador não deixa uma função pura executar I/O por engano.

### Pontos de interação com o mundo externo
Somente em `main`: imprimir os resultados dos testes e devolver o código de
saída do processo. O núcleo (`Alocacao.hs`) nem importa módulos de I/O. Os
próprios testes são **dados** (a lista `casos`, de tuplas
`(nome, descrição, passou?)`) calculados por funções puras; `main` só
imprime essa lista. Isso atende o requisito de separar o núcleo das
operações de entrada e saída.

### Dados imutáveis
`Entregador`, `Pedido`, `Rota` e `Resultado` são `data` sem nenhum
mecanismo de alteração. `atribuir p r` não altera `r`: devolve uma `Rota`
nova (`r { rotaPedidos = ... }` é "cópia com um campo diferente").
`substituirRota` do mesmo jeito: devolve uma lista nova.

### Transformações de dados
O fluxo de `alocar` é uma cadeia de transformações: `partition` (separa
válidos de inválidos), `sortBy` (ordena), `distribuir` (reduz a lista de
pedidos ao par rotas/pendentes). A lista original nunca é tocada.

### Funções de alta ordem e de primeira classe
- `sortBy compararPedidos`, `filter (podeReceber p)`, `map pedPeso`,
  `partition pedidoValido`, `minimumBy (comparing ...)`: todas recebem
  **funções como argumento**.
- `compararPedidos` é uma função passada como valor para `sortBy`
  (função de primeira classe).
- `filter (podeReceber p)` usa **aplicação parcial**: `podeReceber p` é uma
  função nova, que espera só a rota.

### Composição funcional
- `cargaRota = sum . map pedPeso . rotaPedidos`: três funções compostas com `(.)`.
- `compararPedidos = comparing (Down . pedPrioridade) <> comparing pedDistancia <> comparing pedId`:
  a regra de ordenação do contrato é construída **compondo** três
  comparadores pequenos. O `<>` só consulta o próximo critério quando o
  anterior empata, exatamente como pede o contrato (prioridade, distância, id).

### Recursão
`distribuir` é recursiva (de cauda, com acumulador de pendentes). Caso base:
lista de pedidos vazia. Caso recursivo: trata o primeiro pedido e chama a si
mesma com o resto. Substitui o laço `for` com variáveis mutáveis do C e do
Java. (Equivale a um `foldl'`; escrevi a recursão explícita para deixar o
raciocínio visível.)

## 3. Tratamento de erros
- Pedido inválido: `partition pedidoValido` separa antes de qualquer
  alocação, e os inválidos vão para `resInvalidos`.
- "Nenhum entregador serve": `escolherRota` devolve `Maybe Rota`. Em C era
  um `-1` (que o chamador podia esquecer de checar); em Java, uma exceção.
  Em Haskell o tipo `Maybe` obriga o chamador a tratar `Nothing` (o `case`
  precisa cobrir os dois casos).

## 4. Trade-offs honestos
- A atribuição `rotaPedidos r ++ [p]` e a busca por id em `substituirRota`
  custam O(n) cada uma. Para o volume do problema não importa, mas numa
  versão de alto desempenho seriam trocadas por estruturas melhores (ex.: `Data.Map`).
- O código ficou menor que o Java (2 módulos, sem classes nem interfaces) e
  o contrato de ordenação ficou mais declarativo. Em contrapartida, quem está
  acostumado com imperativo precisa se adaptar a ler recursão e composição
  em vez de laços.

# Decisões de Implementação

## [P4-ETAPA-03] Implementação Imperativa (C)

### Estrutura dos arquivos
* `imperativo/alocacao.h` — tipos de dados (`Entregador`, `Pedido`, `ResultadoAlocacao`) e assinaturas dos subprogramas.
* `imperativo/alocacao.c` — lógica de alocação (validação, ordenação, busca de entregador, orquestração).
* `imperativo/testes.c` — executa os 15 casos de teste da Etapa 02 (TC01–TC15) contra a implementação.

### Quais estados são mantidos
* `capacidadeUsada` de cada `Entregador` — quanto da capacidade já foi ocupado durante a execução atual de `alocarEntregas`. É reiniciado a zero no início de cada chamada, para que uma nova alocação nunca herde o estado de uma execução anterior.
* A struct `ResultadoAlocacao` — construída incrementalmente ao longo do processamento: as rotas de cada entregador, a carga total de cada um, a lista de pendentes e a lista de inválidos vão sendo preenchidas conforme os pedidos são processados um a um.

### Quais operações modificam estado
* `entregadores[idx].capacidadeUsada += p.peso` — ocorre dentro de `alocarEntregas`, toda vez que um pedido é atribuído a um entregador.
* `re->idsRota[re->tamRota++] = p.id` e `re->cargaTotal += p.peso` — atualizam a rota e a carga do entregador escolhido.
* `resultado.pendentes[...]` e `resultado.invalidos[...]` — recebem o id do pedido quando ele não pôde ser alocado ou não passou na validação.

### Onde aparecem efeitos colaterais
O principal efeito colateral está em `alocarEntregas`: como em C os vetores são passados por referência (o vetor `entregadores` é o mesmo espaço de memória do chamador), a função **muta diretamente** o campo `capacidadeUsada` de cada entregador. Isso é intencional — representa a ocupação real de cada veículo naquela execução — mas é um efeito colateral explícito, diferente de uma função pura que apenas retornaria um novo resultado sem alterar nada externo. O array de pedidos original, por outro lado, **não é alterado**: a ordenação (`ordenarPedidos`) atua sobre uma cópia local (`validos[]`), para não afetar a ordem em que o chamador enviou os pedidos.

### Quais estruturas de controle foram escolhidas
* `for` — para percorrer entregadores e pedidos (não há necessidade de `while`, já que o número de iterações é sempre conhecido de antemão).
* `if / else` (e `continue`) — para a validação de pedidos e para decidir se um entregador serve ou não para um pedido.
* `qsort` (da biblioteca padrão) com uma função de comparação (`comparaPedidos`) — evita reescrever manualmente um algoritmo de ordenação, mas o critério de comparação em si é expresso de forma totalmente imperativa, com `if` sequenciais.

### Como os subprogramas foram organizados
Cada função tem uma responsabilidade única e recebe todos os dados de que precisa via parâmetros (nenhuma variável global escondida):
1. `pedidoValido(Pedido)` — validação isolada de um único pedido.
2. `comparaPedidos` / `ordenarPedidos` — ordenação determinística dos pedidos válidos.
3. `indiceEntregadorDisponivel` — busca do entregador de menor id com capacidade suficiente.
4. `alocarEntregas` — subprograma orquestrador: chama os anteriores em sequência e vai mutando o estado passo a passo.
5. `imprimirResultado` — apenas exibição, sem nenhuma participação na lógica de decisão (separação entre lógica e I/O).

### Por que a solução é predominantemente imperativa
A lógica é expressa como uma sequência explícita de passos que alteram estado ao longo do tempo (`for` percorrendo e mutando `capacidadeUsada`, contadores `tamRota`/`nPendentes` sendo incrementados), e não como composição de funções puras ou recursão. Não há classes, não há polimorfismo, não há métodos amarrados a objetos — `Entregador` e `Pedido` são apenas `struct`s de dados, manipuladas de fora por funções que operam sobre ponteiros/arrays passados explicitamente. A ordem de execução (`ordenar` → `alocar`, um pedido de cada vez, mutando o array de entregadores a cada passo) é a própria essência da solução — é assim, e não de outro jeito, que o programa "pensa".

### Validação
Os 15 casos de teste definidos em `testes/casos.md` (Etapa 02) foram implementados em `imperativo/testes.c` e todos passam (15/15), incluindo os 2 casos de entrada inválida (TC14, TC15), que confirmam que o sistema rejeita pedidos malformados em vez de processá-los.

# Reflexão — Etapa 04 (Orientado a Objetos)

## Como meu modelo mudou ao passar do paradigma imperativo para o orientado a objetos?

Na Etapa 03 (C), a solução era uma sequência de funções livres (`pedidoValido`, `ordenarPedidos`, `indiceEntregadorDisponivel`, `alocarEntregas`) operando sobre `struct`s passivas. Nenhum dado "sabia" nada sobre si mesmo — toda a inteligência estava fora, em funções que recebiam ponteiros/arrays e os mutavam.

Na Etapa 04 (Java), a mesma lógica foi redistribuída entre objetos que carregam sua própria responsabilidade. Isso mudou o modelo em vários pontos concretos:

### Representação do estado
Antes: o estado (`capacidadeUsada` de cada entregador) era um campo de uma `struct` qualquer, mutado por código externo (`entregadores[idx].capacidadeUsada += p.peso`). Qualquer função podia acessar e alterar esse campo diretamente.

Agora: `cargaAtual` e `rota` são campos **privados** de `Entregador`. Ninguém de fora consegue mutá-los diretamente — só através do método público `atribuir(Pedido)`, que primeiro valida se o pedido cabe. O estado é o mesmo, mas quem controla o acesso a ele mudou.

### Responsabilidades
Antes: uma única função (`alocarEntregas`) concentrava validação, ordenação e decisão de alocação, chamando subfunções auxiliares.

Agora: cada responsabilidade tem um dono. `Pedido` valida a si mesmo (`isValido()`). `Entregador` decide se comporta um pedido (`podeReceber`) e se atualiza (`atribuir`). `CriterioOrdenacaoPedidos` só sabe comparar dois pedidos. `MenorIdDisponivel` só sabe escolher um entregador. `SistemaAlocacao` não faz nada sozinho — só orquestra, delegando cada decisão ao objeto responsável por ela.

### Relacionamento entre componentes
Antes: os "relacionamentos" eram só parâmetros de função — arrays passados de uma função pra outra, sem nenhuma estrutura declarada entre eles.

Agora: existem relações de **agregação** (`SistemaAlocacao` agrega uma `List<Entregador>` — os entregadores existem independentemente dele e continuam existindo depois) e de **composição/posse** (cada `Entregador` é dono da sua própria `List<Pedido> rota` — a rota não faz sentido fora do entregador a quem pertence). Além disso, `SistemaAlocacao` depende de duas abstrações (`Comparator<Pedido>` e `EstrategiaAlocacao`) recebidas por injeção no construtor, em vez de ter a lógica de ordenação e escolha "grudada" nele.

### Reutilização e extensão do sistema
Antes: pra mudar o critério de desempate ou a estratégia de escolha do entregador, era preciso editar o corpo de `comparaPedidos` ou `indiceEntregadorDisponivel` diretamente.

Agora: `CriterioOrdenacaoPedidos` e `MenorIdDisponivel` são implementações concretas de interfaces (`Comparator<Pedido>` e `EstrategiaAlocacao`). Uma nova regra de desempate ou uma nova estratégia de escolha (por exemplo, "sempre o entregador com mais espaço livre") pode ser adicionada como uma **nova classe**, sem tocar em `SistemaAlocacao`. Isso é o princípio aberto/fechado: o sistema fica aberto pra extensão e fechado pra modificação.

### Encapsulamento
Antes: os campos da `struct Entregador` eram todos públicos (é assim que `struct` funciona em C) — não havia proteção nenhuma contra uso incorreto.

Agora: os campos de `Entregador` e `Pedido` são privados. `Pedido` nem tem setters — é imutável por design. `Entregador.atribuir()` recusa (lança exceção) um pedido que não caiba, em vez de simplesmente confiar que quem chama já checou isso antes — o objeto se protege sozinho.

## Sobre herança

O enunciado pede que a solução demonstre herança **quando isso fizer sentido**, e permite explicitamente justificar o uso de composição no lugar dela. Optei por **não usar herança** nesta solução, por dois motivos:

1. **Não existe uma hierarquia natural no domínio.** O candidato mais óbvio seria subclasses de `Entregador` por modal de transporte (ex.: `EntregadorMoto`, `EntregadorCarro`), mas isso foi explicitamente excluído do escopo já na Etapa 01 (restrição: "sem suporte a múltiplos modais de transporte dinâmicos"). Criar uma hierarquia de classes só para satisfazer o requisito, sem que o domínio realmente peça por isso, seria herança artificial — exatamente o que o enunciado pede para evitar.
2. **Composição e agregação já resolvem os problemas de reuso e extensão** que herança resolveria neste caso: a troca de comportamento (critério de ordenação, estratégia de alocação) é feita trocando o objeto injetado, não estendendo uma classe. É o princípio "prefira composição a herança", aplicado de forma consciente, não por padrão.

Se o domínio crescesse — por exemplo, se entrasse em escopo o suporte a diferentes tipos de veículo com regras de capacidade distintas —, aí sim uma hierarquia `Veiculo` / `VeiculoTerrestre` / `VeiculoAereo` (ou uma interface `Veiculo` implementada por tipos concretos) passaria a fazer sentido de verdade.

# [P4-ETAPA-05] Comparação entre Imperativo e POO

## Sistema de Gestão e Alocação de Rotas de Entrega

## 1. Análise comparativa

### Representação do estado
Em C, o estado (`capacidadeUsada` de cada entregador) é só um campo de `struct`, e qualquer função que receba o array de entregadores consegue mexer nele direto. Em Java, esse mesmo estado (`cargaAtual` e `rota`) é privado dentro de `Entregador`, então ninguém de fora acessa sem passar pelo método `atribuir()`.

### Mutabilidade
Na versão em C tudo é mutável, não tem distinção nenhuma — a `struct Pedido` e a `struct Entregador` são iguais nesse sentido. Na versão em Java, `Pedido` foi feita imutável de propósito (campos `final`, sem setter), e só `Entregador` é mutável, porque faz sentido pra ele: sua carga muda conforme ele recebe pedidos.

### Fluxo de controle
Em C o fluxo é bem linear: a função `alocarEntregas` faz tudo em sequência, chamando as outras funções uma atrás da outra. Em Java o fluxo fica mais espalhado: `SistemaAlocacao.alocar()` delega pra `Entregador.podeReceber()`, `Entregador.atribuir()`, pro `Comparator` e pra `EstrategiaAlocacao`. Pra entender o fluxo completo em Java precisa pular entre mais arquivos.

### Decomposição do problema
Em C o problema foi quebrado por **tarefa** (validar, ordenar, buscar, alocar — cada uma uma função). Em Java foi quebrado por **entidade/responsabilidade** (Pedido, Entregador, critério de ordenação, estratégia de escolha). São dois jeitos diferentes de cortar o mesmo problema.

### Reutilização
Em C, pra reusar a lógica de ordenação em outro contexto, dá pra chamar `ordenarPedidos` de qualquer lugar — mas se quiser um critério diferente, tem que editar `comparaPedidos`. Em Java dá pra criar um novo `Comparator<Pedido>` e passar pro `SistemaAlocacao` sem mexer em mais nada — a reutilização por troca de objeto é mais direta.

### Manutenção
Em C, como os campos são todos públicos, um bug de "alguém mexeu na capacidadeUsada sem querer" é possível em qualquer lugar do código. Em Java isso é mais difícil de acontecer porque o campo é privado — só o próprio `Entregador` pode alterar seu estado.

### Tratamento de erros
C não tem exceção, então a validação de pedido inválido só existe como um `if` que separa os pedidos em "válidos" e "inválidos" — não tem como travar em tempo de execução se alguém usar errado. Em Java, além dessa mesma validação, o `Entregador.atribuir()` lança uma exceção (`IllegalStateException`) se tentarem atribuir um pedido que não cabe — é uma camada extra de proteção que o C não tinha.

### Efeitos colaterais
Os dois têm efeito colateral (mutação de estado), isso não muda. A diferença é *onde* ele acontece: em C é externo (uma função de fora mexe no array de entregadores), em Java é interno (o próprio objeto muda a si mesmo através de um método seu).

### Facilidade para testar
Os dois foram testados do mesmo jeito (um `main` rodando os 15 casos da Etapa 02, sem framework de teste). Em Java os testes ficaram um pouco mais legíveis porque dá pra perguntar direto pro objeto (`e1.getRota()`, `e1.getCargaAtual()`) em vez de acessar campos de struct.

### Organização do código
C ficou em 3 arquivos (header, implementação, testes). Java ficou em 8 arquivos (um por classe/interface). Pra um problema desse tamanho, isso é bem mais arquivo pra abrir e navegar.

### Complexidade
Os dois resolvem o problema com a mesma complexidade de algoritmo (ordenar + percorrer). A diferença é de complexidade acidental — o Java tem mais estrutura (interfaces, classes separadas) do que o problema, no tamanho que ele tem agora, realmente pede.


## 2. Perguntas

**1. Qual problema ficou mais fácil de expressar de forma imperativa?**
A lógica de alocação em si — validar, ordenar, percorrer e ir preenchendo capacidade — é basicamente uma receita de passos, e isso mapeia direto pra uma sequência de `for`/`if`. Não precisei decidir de quem é a responsabilidade de fazer isso, só escrevi o passo a passo.

**2. Qual problema ficou mais fácil de expressar utilizando orientação a objetos?**
Garantir que o estado de um entregador não fosse corrompido por acidente. Em Java, `Entregador` é o único dono do próprio estado — não tem como esquecer de resetar `capacidadeUsada` em algum lugar do código, porque só existe um lugar (`reiniciar()`) que faz isso.

**3. Onde a orientação a objetos realmente trouxe vantagem?**
Na extensibilidade: pra trocar a regra de escolha do entregador ou o critério de ordenação, é só trocar o objeto passado pro `SistemaAlocacao`, sem editar a lógica existente. E na proteção contra erro: o `atribuir()` se recusa sozinho a aceitar um pedido que não cabe.

**4. Em quais situações a utilização de objetos acrescentou complexidade desnecessária?**
A interface `EstrategiaAlocacao` com uma única implementação (`MenorIdDisponivel`) é over-engineering pro tamanho atual do problema — em C a mesma regra é uma função de 15 linhas. 8 arquivos pra resolver o mesmo problema que 3 arquivos resolvem em C também é mais estrutura do que o problema, hoje, realmente precisa.

**5. Que partes do problema praticamente não mudaram entre as duas implementações?**
A regra de negócio em si: prioridade decrescente, depois distância crescente, depois id crescente pro desempate; e a condição de capacidade (`peso <= capacidade restante`). Essas regras são idênticas nas duas versões — só mudou onde elas moram no código.

**6. Que partes precisaram ser completamente remodeladas?**
A forma de guardar o resultado da alocação. Em C precisei de uma struct `ResultadoAlocacao` com um vetor `porEntregador[]` em paralelo aos entregadores, porque a struct `Entregador` não tinha como "saber" sua própria rota. Em Java isso sumiu — cada `Entregador` já é dono da sua própria rota, então o resultado só precisa carregar pendentes e inválidos. Foi a mudança estrutural mais profunda entre as duas versões.
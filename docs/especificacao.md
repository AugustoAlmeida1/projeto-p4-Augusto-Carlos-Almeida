# [P4-ETAPA-01] Proposta e Especificação do Problema

## Sistema de Gestão e Alocação de Rotas de Entrega

### 1. Descrição do problema
Empresas de logística urbana precisam organizar pedidos de entrega, atribuí-los a entregadores disponíveis e definir a ordem em que cada entregador visita seus destinos. O sistema recebe pedidos com peso, prioridade e localização, e entregadores com capacidade de carga, e deve decidir quem entrega o que e em qual ordem.

### 2. Objetivo
O sistema deve cadastrar entregadores e pedidos, alocar pedidos aos entregadores respeitando o limite de capacidade de carga, e ordenar as entregas de cada entregador seguindo uma regra simples e determinística: prioridade mais alta primeiro; em caso de empate, o destino mais próximo primeiro.

### 3. Entradas
* Lista de entregadores (ID, capacidade máxima de carga, status de disponibilidade).
* Lista de pedidos (ID, peso, prioridade de 1 a 3, distância até o entregador ou até a última parada).

### 4. Saídas
* Lista de pedidos atribuídos a cada entregador.
* Sequência ordenada de entregas (rota) para cada entregador.
* Carga total alocada por entregador.
* Lista de pedidos pendentes que não couberam na capacidade de nenhum entregador disponível.

### 5. Regras do problema
* Um pedido só pode ser atribuído a um entregador.
* A soma dos pesos dos pedidos de um entregador não pode ultrapassar sua capacidade máxima.
* Pedidos são alocados em ordem decrescente de prioridade (3 antes de 2, antes de 1).
* Dentro da mesma prioridade, o pedido mais próximo é atendido primeiro.
* Se um pedido não couber em nenhum entregador disponível, ele vai para a lista de pendentes.
* Não há recálculo de rota “ótima” (sem otimização combinatória) — a ordenação segue só a regra acima, aplicada uma vez.

### 6. Casos de exemplo
* **Exemplo 1:** Entregador A (10kg). Pedido 1 (2kg, prioridade 1), Pedido 2 (5kg, prioridade 3).
  * *Saída:* Rota = [Pedido 2, Pedido 1]. Carga total: 7kg.

* **Exemplo 2:** Entregador B (5kg). Pedido 3 (8kg, prioridade 2).
  * *Saída:* Pedido 3 fica pendente (excede capacidade).

* **Exemplo 3:** Nenhum entregador disponível. 3 pedidos cadastrados.
  * *Saída:* Lista de rotas vazia; os 3 pedidos ficam pendentes.

* **Exemplo 4:** Entregador C (15kg). Pedido 4 (5kg, prioridade 2, distância 8km), Pedido 5 (5kg, prioridade 2, distância 3km).
  * *Saída:* Rota = [Pedido 5, Pedido 4] (mesma prioridade, desempate por distância).

* **Exemplo 5:** Entregador D (10kg). Pedido 6 (10kg, prioridade 3).
  * *Saída:* Rota = [Pedido 6]. Carga total: 10kg (capacidade máxima atingida).

### 7. Casos-limite
* Capacidade exata: peso total dos pedidos alocados é igual à capacidade máxima do entregador.
* Empate total: dois pedidos com mesma prioridade e mesma distância (o sistema pode desempatar por ordem de chegada/ID).
* Entrada vazia: execução sem nenhum pedido ou nenhum entregador cadastrado.

### 8. Restrições
* Sem rastreamento GPS em tempo real.
* Sem interface gráfica (GUI) — só lógica de processamento.
* Sem otimização combinatória de rota (não será calculado o menor percurso total possível entre todos os pontos) — a ordenação usa apenas a regra de prioridade + distância descrita acima.
* Sem suporte a múltiplos modais de transporte.

### 9. Principais conceitos do domínio
* Entregador: agente responsável pelo transporte, com capacidade de carga.
* Pedido: item a ser entregue, com peso, prioridade e distância.
* Rota: sequência ordenada de pedidos atribuídos a um entregador.
* Alocação: vínculo entre um pedido e um entregador.

### 10. Adequação aos quatro paradigmas
* Imperativo: Porque é natural para percorrer a lista de pedidos com laços, acumular carga em variáveis e decidir a alocação passo a passo.
* Orientado a Objetos: Porque é natural para modelar Entregador, Pedido e Rota como classes com atributos e métodos (podeReceber(), adicionarPedido()).
* Funcional: (*Previsto*): Porque a alocação e a ordenação são essencialmente filter (quem cabe na capacidade) + sort/reduce (por prioridade e distância).
* Lógico: (*Previsto*): Porque a regra “pedido cabe no entregador se peso ≤ capacidade restante” e a ordem de prioridade se expressam bem como fatos e cláusulas de restrição em Prolog.

### 11. Linguagens inicialmente consideradas
* Imperativo — C: vetores/structs e laços explícitos para simular a alocação.
* Orientado a Objetos — Java: classes Entregador, Pedido, Rota com encapsulamento.
* Funcional — (*Previsto*) Haskell: listas de pedidos processadas com filter, sortBy e recursão.
* Lógico — (*Previsto*) Prolog: fatos para pedidos/entregadores e regras de restrição de capacidade e prioridade. 
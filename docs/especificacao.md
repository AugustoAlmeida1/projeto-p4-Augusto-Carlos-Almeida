# [P4-ETAPA-01] Proposta e Especificação do Problema

## Sistema de Gestão e Recomendação de Rotas de Entrega


### 1. Descrição do problema
Empresas de logística urbana enfrentam dificuldades para organizar pacotes, atribuir entregas a entregadores disponíveis e determinar a melhor sequência de paradas para minimizar a distância percorrida. O sistema deve receber pedidos de entrega com localizações e prioridades, manter o cadastro dos entregadores e calcular a melhor alocação de entregas.

### 2. Objetivo
O sistema deve registrar pedidos de entrega e entregadores, verificar quais entregadores estão disponíveis para atender determinados pacotes com base em capacidade de carga e rota, e ordenar as entregas para otimizar o tempo e percurso total.

### 3. Entradas
* Lista de entregadores (ID, capacidade máxima de carga, status de disponibilidade).
* Lista de pacotes/pedidos (ID, peso, prioridade de 1 a 3, coordenadas de origem e destino).
* Matriz de distâncias/tempos entre pontos de entrega.

### 4. Saídas
* Atribuição de um grupo de pacotes para cada entregador disponível.
* Sequência ordenada de paradas (rota) para cada entregador.
* Distância total calculada e tempo estimado da rota.
* Lista de pedidos pendentes que não puderam ser alocados devido ao limite de capacidade.

### 5. Regras do problema
* Um pacote só pode ser alocado a um entregador disponível.
* A soma dos pesos dos pacotes atribuídos a um entregador não pode exceder a capacidade máxima do seu veículo.
* Pedidos com prioridade maior (ex: valor 3) devem ter preferência de atendimento sobre prioridades menores.
* O cálculo da rota deve preferir o menor percurso total possível acumulado.

### 6. Casos de exemplo
* **Exemplo 1:** Entregador A (Capacidade: 10kg). Pacote 1 (2kg, Prioridade 1), Pacote 2 (5kg, Prioridade 3).
  * *Saída:* Entregador A recebe Pacote 2 primeiro, depois Pacote 1. Carga total: 7kg.
* **Exemplo 2:** Entregador B (Capacidade: 5kg). Pacote 3 (8kg, Prioridade 2).
  * *Saída:* Pacote 3 fica na lista de pendentes (excede a capacidade).
* **Exemplo 3:** Sem entregadores disponíveis. 3 Pacotes pendentes.
  * *Saída:* Lista de rotas vazia; todos os 3 pacotes mantidos em espera.
* **Exemplo 4:** Entregador C (Capacidade: 15kg). Pacote 4 (5kg, Prioridade 2, Local X), Pacote 5 (5kg, Prioridade 2, Local Y mais próximo).
  * *Saída:* Entregador C recebe Pacote 5 e depois Pacote 4 (otimização por menor distância).
* **Exemplo 5:** Entregador D (Capacidade: 10kg). Pacote 6 (10kg, Prioridade 3).
  * *Saída:* Entregador D recebe Pacote 6. Carga total: 10kg (100% ocupado).

### 7. Casos-limite
* **Capacidade Exata:** O peso total dos pacotes é exatamente igual à capacidade máxima do entregador (ex: 10kg de carga para 10kg de capacidade).
* **Empate de Prioridade e Distância:** Dois pacotes possuem a mesma prioridade e a mesma distância em relação ao ponto atual do entregador.
* **Entrada Vazia:** Execução do sistema sem nenhum pedido cadastrado ou sem nenhum entregador ativo na base de dados.

### 8. Restrições
* Não haverá rastreamento via GPS em tempo real (telemetria contínua está fora do escopo).
* Não haverá interface gráfica (GUI); o sistema trata apenas a lógica de processamento de dados e regras de negócio.
* Não haverá suporte para múltiplos modais de transporte dinâmicos (como variação automática entre bicicleta, carro e caminhão além do limite de peso estático).

### 9. Principais conceitos do domínio
* **Entregador:** Agente responsável por realizar o transporte.
* **Pacote/Pedido:** Item a ser entregue com peso, prioridade e destino.
* **Rota:** Sequência ordenada de locais de parada.
* **Atribuição:** Vínculo estabelecido entre um pacote e um entregador.

### 10. Adequação aos quatro paradigmas
* **Imperativo:** Adequado para controle passo a passo de estados, loops de iteração sobre vetores de pedidos e manipulação explícita da memória durante a montagem das rotas.
* **Orientado a Objetos:** Ideal para modelar os conceitos do domínio como entidades com atributos e comportamentos bem definidos (`Entregador`, `Pacote`, `Rota`).
* **Funcional:** Como uma prévia: Porque permite tratar coleções de pacotes através de funções puras, operações de mapeamento, filtragem (`filter`, `map`, `reduce`) e recursão para calcular rotas sem efeitos colaterais.
* **Lógico:** Como uma prévia: Porque é usado para expressar regras e restrições de alocação através de fatos e cláusulas ("Se o pacote P tem peso W, e o veículo V tem capacidade C, e W <= C, então a entrega é válida").

### 11. Linguagens inicialmente consideradas
* **Imperativo — C:** Escolhido pela facilidade em demonstrar controle manual de estruturas de dados e loops sequenciais explícitos.
* **Orientado a Objetos — Java:** Escolhido pelo suporte robusto a encapsulamento, classes e abstração das entidades do sistema.
* **Funcional — Haskell:** Previsto: escolhido pelo forte suporte à imutabilidade e facilidade no processamento de listas via funções de alta ordem.
* **Lógico — Prolog:** Previsto: escolhido por ser o padrão do paradigma lógico, permitindo declarar as regras de validação como relações diretas.
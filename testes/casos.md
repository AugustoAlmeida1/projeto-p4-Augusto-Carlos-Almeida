# [P4-ETAPA-02] Contrato Semântico e Testes

## Sistema de Gestão e Alocação de Rotas de Entrega

### 1. Contrato semântico
**Função principal:** alocarEntregas(entregadores, pedidos) -> (rotas, pendentes)

**Tipos de entrada**
* Entregador(id, capacidade, disponivel) — id inteiro positivo único; capacidade numérica > 0; disponivel booleano.
* Pedido(id, peso, prioridade, distancia) — id inteiro positivo único; peso numérico > 0; prioridade inteiro ∈ {1, 2, 3}; distancia numérica ≥ 0.

**Pré-condições**
* Todos os IDs de entregadores são únicos entre si; todos os IDs de pedidos são únicos entre si.
* Todo Pedido processado deve ter peso > 0 e prioridade ∈ {1, 2, 3}. Um pedido que viole isso é inválido e deve ser rejeitado pelo sistema (não entra na alocação nem nos pendentes — é reportado como erro de entrada).

**Regras de comportamento (pós-condições)**
* Somente entregadores com disponivel = true participam da alocação. Entregadores indisponíveis não aparecem na saída.
* Os pedidos válidos são ordenados globalmente por: (a) prioridade decrescente; (b) em empate, distancia crescente; (c) em novo empate, id crescente.
* Percorrendo essa ordem, cada pedido é atribuído ao entregador disponível de menor id que ainda tenha capacidade restante suficiente (peso do pedido ≤ capacidade restante do entregador).
* Se nenhum entregador disponível tiver capacidade suficiente para um pedido, ele vai para a lista de pendentes.
* A rota de cada entregador é a sequência de pedidos que lhe foram atribuídos, na ordem em que a atribuição ocorreu (passo 2/3).
* A carga total de um entregador é a soma dos pesos dos pedidos em sua rota, e nunca ultrapassa sua capacidade.
* Não há recálculo de rota ótima nem otimização combinatória — a ordenação é aplicada uma única vez (determinística).

**Saída**
* Para cada entregador disponível: rota (lista ordenada de IDs de pedidos) e carga total.
* Lista de IDs de pedidos pendentes.
* Pedidos inválidos são sinalizados separadamente como erro, fora do fluxo normal de rotas/pendentes.


### 2. Casos de teste
Notação: E(id, capacidade, disponivel) e P(id, peso, prioridade, distancia).

#### Casos normais
| ID | Entrada | Saída esperada | Descrição |
|----|---------|-----------------|-----------|
| TC01 | E(1,10,true); P(1,5,2,3) | Rota E1=[P1], carga=5, pendentes=[] | Alocação simples, um pedido cabe em um entregador |
| TC02 | E(1,20,true); P(1,5,1,5), P(2,5,3,10) | Rota E1=[P2,P1], carga=10 | Prioridade mais alta é atendida primeiro, mesmo estando mais distante |
| TC03 | E(1,20,true); P(1,4,2,8), P(2,4,2,3) | Rota E1=[P2,P1], carga=8 | Mesma prioridade: desempate pela menor distância |
| TC04 | E(1,5,true); P(1,8,2,1) | Rota E1=[], carga=0, pendentes=[P1] | Pedido excede a capacidade do único entregador |
| TC05 | E(1,10,true), E(2,10,true); P(1,6,2,1), P(2,6,2,1) | Rota E1=[P1], Rota E2=[P2], pendentes=[] | Pedido que não cabe mais no primeiro entregador é realocado ao próximo disponível |
| TC06 | E(1,10,false), E(2,10,true); P(1,5,2,1) | Rota E2=[P1], pendentes=[]; | E1 não aparece na saída Entregador indisponível é ignorado na alocação |
| TC07 | E(1,10,true); P(1,7,3,2), P(2,5,1,1) | Rota E1=[P1], carga=7, pendentes=[P2] | Prioridade alta consome a capacidade, deixando o de prioridade baixa pendente |
| TC08 | E(1,8,true), E(2,8,true); P(1,5,3,5), P(2,5,3,2), P(3,3,1,1) | Rota E1=[P2,P3], carga=8; Rota E2=[P1], carga=5; pendentes=[] | Combinação de prioridade, desempate por distância e distribuição entre dois entregadores |
| TC09 | E(1,5,true), E(2,5,true); P(1,6,3,1), P(2,4,2,1) | Rota E1=[P2], carga=4; Rota E2=[]; pendentes=[P1] | Pedido de maior prioridade fica pendente por exceder a capacidade de todos os entregadores |
| TC10 | E(1,5,true), E(2,5,true); P(1,5,2,1), P(2,5,2,2) | Rota E1=[P1], carga=5; Rota E2=[P2], carga=5; pendentes=[] | Dois pedidos preenchem exatamente dois entregadores diferentes |

#### Casos-limite
| ID | Entrada | Saída esperada | Descrição |
|----|---------|-----------------|-----------|
| TC11 | E(1,10,true); P(1,6,2,1), P(2,4,2,2) | Rota E1=[P1,P2], carga=10, pendentes=[] | Capacidade exata: soma dos pesos alocados é igual à capacidade máxima |
| TC12 | E(1,20,true); P(1,3,2,5), P(2,3,2,5) | Rota E1=[P1,P2], pendentes=[] | Empate total (mesma prioridade e mesma distância): desempate pelo menor ID |
| TC13 | Nenhum entregador; nenhum pedido | Rotas vazias, pendentes=[] | Entrada totalmente vazia|

#### Casos de entrada inválida
| ID | Entrada | Saída esperada | Descrição |
|----|---------|-----------------|-----------|
| TC14 | E(1,10,true); P(1,5,5,1) | Erro de validação: prioridade fora do intervalo {1,2,3}; pedido não processado | Prioridade inválida |
| TC15 | E(1,10,true); P(1,-2,2,1) | Erro de validação: peso deve ser > 0; pedido não processado | Peso inválido (não positivo) |
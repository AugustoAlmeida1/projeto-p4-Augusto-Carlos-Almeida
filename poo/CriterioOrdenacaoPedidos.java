import java.util.Comparator;

/**
 * Encapsula, como objeto, a regra de ordenação do contrato semântico
 * (Etapa 02): prioridade decrescente; empate por distância crescente;
 * novo empate por id crescente.
 *
 * Decisão de modelagem: em vez de uma função de comparação solta (como
 * o comparaPedidos da Etapa 03), a regra vira um objeto que implementa
 * a interface padrão Comparator<Pedido> do Java. Isso é polimorfismo
 * de verdade: List.sort(Comparator<T>) não sabe (nem precisa saber)
 * qual é a regra concreta — ele só chama compare(). Se um dia o
 * critério mudar, basta trocar o objeto passado, sem tocar em quem
 * ordena.
 */
public class CriterioOrdenacaoPedidos implements Comparator<Pedido> {

    @Override
    public int compare(Pedido a, Pedido b) {
        if (a.getPrioridade() != b.getPrioridade()) {
            return b.getPrioridade() - a.getPrioridade();
        }
        if (a.getDistancia() != b.getDistancia()) {
            return Double.compare(a.getDistancia(), b.getDistancia());
        }
        return Integer.compare(a.getId(), b.getId());
    }
}

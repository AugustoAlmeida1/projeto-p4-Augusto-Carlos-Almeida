import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Orquestra a alocação de pedidos aos entregadores.
 *
 * Decisão de modelagem: SistemaAlocacao AGREGA uma lista de
 * Entregador (eles existem independentemente dele) e COMPÕE/recebe
 * por injeção o critério de ordenação e a estratégia de alocação.
 * Ele não sabe COMO cada um desses colaboradores faz seu trabalho —
 * só sabe chamá-los na ordem certa. Essa é a essência do modelo OO
 * aqui: responsabilidades distribuídas entre objetos especializados,
 * em vez de uma única função fazendo tudo (como alocarEntregas na
 * Etapa 03).
 */
public class SistemaAlocacao {
    private final List<Entregador> entregadores;
    private final Comparator<Pedido> criterioOrdenacao;
    private final EstrategiaAlocacao estrategia;

    public SistemaAlocacao(List<Entregador> entregadores,
                            Comparator<Pedido> criterioOrdenacao,
                            EstrategiaAlocacao estrategia) {
        this.entregadores = entregadores;
        this.criterioOrdenacao = criterioOrdenacao;
        this.estrategia = estrategia;
    }

    public ResultadoAlocacao alocar(List<Pedido> pedidos) {
        for (Entregador e : entregadores) {
            e.reiniciar();
        }

        List<Pedido> validos = new ArrayList<>();
        List<Pedido> invalidos = new ArrayList<>();
        for (Pedido p : pedidos) {
            if (p.isValido()) {
                validos.add(p);
            } else {
                invalidos.add(p);
            }
        }

        validos.sort(criterioOrdenacao);

        List<Pedido> pendentes = new ArrayList<>();
        for (Pedido p : validos) {
            Optional<Entregador> escolhido = estrategia.escolher(entregadores, p);
            if (escolhido.isPresent()) {
                escolhido.get().atribuir(p);
            } else {
                pendentes.add(p);
            }
        }

        return new ResultadoAlocacao(entregadores, pendentes, invalidos);
    }
}

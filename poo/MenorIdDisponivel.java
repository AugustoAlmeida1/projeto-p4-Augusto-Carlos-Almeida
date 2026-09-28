import java.util.List;
import java.util.Optional;

/**
 * Estratégia concreta do contrato semântico (Etapa 02, regra 3):
 * entre os entregadores disponíveis com capacidade suficiente,
 * escolhe o de menor id.
 */
public class MenorIdDisponivel implements EstrategiaAlocacao {

    @Override
    public Optional<Entregador> escolher(List<Entregador> entregadores, Pedido pedido) {
        Entregador melhor = null;
        for (Entregador e : entregadores) {
            if (e.podeReceber(pedido)) {
                if (melhor == null || e.getId() < melhor.getId()) {
                    melhor = e;
                }
            }
        }
        return Optional.ofNullable(melhor);
    }
}

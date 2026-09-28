import java.util.List;
import java.util.Optional;

/**
 * Abstrai "como escolher um entregador para um pedido".
 *
 * Decisão de modelagem: isolar essa decisão numa interface (em vez de
 * deixá-la dentro do orquestrador, como a função
 * indiceEntregadorDisponivel da Etapa 03) permite trocar a estratégia
 * de alocação inteira sem alterar SistemaAlocacao — é o princípio
 * aberto/fechado (aberto para extensão, fechado para modificação).
 */
public interface EstrategiaAlocacao {
    Optional<Entregador> escolher(List<Entregador> entregadores, Pedido pedido);
}

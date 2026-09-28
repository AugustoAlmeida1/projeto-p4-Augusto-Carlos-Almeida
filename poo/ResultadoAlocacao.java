import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resultado de uma execução de SistemaAlocacao.alocar(...).
 *
 * Decisão de modelagem: repare que NÃO existe aqui um "porEntregador[]"
 * paralelo como na Etapa 03. Isso não é uma simplificação — é uma
 * consequência direta do modelo OO: cada Entregador já é dono da sua
 * própria rota (getRota()), então o resultado só precisa carregar o
 * que não pertence a nenhum entregador: pendentes e inválidos.
 */
public class ResultadoAlocacao {
    private final List<Entregador> entregadores;
    private final List<Pedido> pendentes;
    private final List<Pedido> invalidos;

    public ResultadoAlocacao(List<Entregador> entregadores, List<Pedido> pendentes, List<Pedido> invalidos) {
        this.entregadores = new ArrayList<>(entregadores);
        this.pendentes = new ArrayList<>(pendentes);
        this.invalidos = new ArrayList<>(invalidos);
    }

    public List<Entregador> getEntregadoresDisponiveis() {
        return entregadores.stream()
                .filter(Entregador::isDisponivel)
                .collect(Collectors.toList());
    }

    public List<Pedido> getPendentes() {
        return Collections.unmodifiableList(pendentes);
    }

    public List<Pedido> getInvalidos() {
        return Collections.unmodifiableList(invalidos);
    }
}

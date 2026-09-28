/**
 * Representa um pedido de entrega.
 *
 * Decisão de modelagem: Pedido é IMUTÁVEL (todos os campos são finais,
 * não há setters). Diferente da struct Pedido da Etapa 03 (que era só
 * um agrupamento de dados manipulado de fora), aqui o próprio Pedido
 * sabe se é válido — a responsabilidade de validação é dele, não de
 * uma função externa.
 */
public final class Pedido {
    private final int id;
    private final double peso;
    private final int prioridade;
    private final double distancia;

    public Pedido(int id, double peso, int prioridade, double distancia) {
        this.id = id;
        this.peso = peso;
        this.prioridade = prioridade;
        this.distancia = distancia;
    }

    /** Regra do contrato semântico (Etapa 02): peso > 0 e prioridade em {1,2,3}. */
    public boolean isValido() {
        return peso > 0 && prioridade >= 1 && prioridade <= 3;
    }

    public int getId() {
        return id;
    }

    public double getPeso() {
        return peso;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public double getDistancia() {
        return distancia;
    }

    @Override
    public String toString() {
        return "P" + id;
    }
}

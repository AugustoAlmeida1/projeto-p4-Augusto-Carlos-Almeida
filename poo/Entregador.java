import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa um entregador.
 *
 * Decisão de modelagem: diferente da Etapa 03 (onde um código externo
 * mutava diretamente o campo capacidadeUsada de uma struct), aqui o
 * Entregador é dono do seu próprio estado (cargaAtual e rota) e é o
 * único responsável por decidir se pode ou não receber um pedido, e
 * por se atualizar quando recebe um. Ninguém de fora consegue mutar
 * esse estado diretamente — só através dos métodos públicos
 * (encapsulamento).
 */
public class Entregador {
    private final int id;
    private final double capacidadeMaxima;
    private final boolean disponivel;

    private double cargaAtual;
    private final List<Pedido> rota;

    public Entregador(int id, double capacidadeMaxima, boolean disponivel) {
        this.id = id;
        this.capacidadeMaxima = capacidadeMaxima;
        this.disponivel = disponivel;
        this.cargaAtual = 0.0;
        this.rota = new ArrayList<>();
    }

    /** Responsabilidade do próprio objeto: decidir se comporta um pedido. */
    public boolean podeReceber(Pedido pedido) {
        if (!disponivel) {
            return false;
        }
        double restante = capacidadeMaxima - cargaAtual;
        return restante >= pedido.getPeso();
    }

    /**
     * Atribui um pedido a este entregador, mutando seu próprio estado.
     * Lança exceção se o pedido não couber — proteção contra uso
     * incorreto da classe (o chamador deve checar podeReceber antes).
     */
    public void atribuir(Pedido pedido) {
        if (!podeReceber(pedido)) {
            throw new IllegalStateException(
                "Entregador " + id + " nao pode receber o pedido " + pedido.getId());
        }
        rota.add(pedido);
        cargaAtual += pedido.getPeso();
    }

    /** Reinicia o estado do entregador para uma nova execução de alocação. */
    public void reiniciar() {
        rota.clear();
        cargaAtual = 0.0;
    }

    public int getId() {
        return id;
    }

    public double getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public double getCargaAtual() {
        return cargaAtual;
    }

    /** Cópia somente-leitura: quem recebe essa lista não consegue alterar a rota real. */
    public List<Pedido> getRota() {
        return Collections.unmodifiableList(rota);
    }

    @Override
    public String toString() {
        return "E" + id;
    }
}

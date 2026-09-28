import java.util.Arrays;
import java.util.List;

public class TestesAlocacao {

    private static int totalTestes = 0;
    private static int testesOk = 0;

    private static boolean idsIguais(List<Pedido> lista, int... esperados) {
        if (lista.size() != esperados.length) {
            return false;
        }
        for (int i = 0; i < esperados.length; i++) {
            if (lista.get(i).getId() != esperados[i]) {
                return false;
            }
        }
        return true;
    }

    private static void checar(String tc, boolean condicao, String descricao) {
        totalTestes++;
        if (condicao) {
            testesOk++;
            System.out.println("[OK ] " + tc + " - " + descricao);
        } else {
            System.out.println("[FAI] " + tc + " - " + descricao);
        }
    }

    private static SistemaAlocacao criarSistema(List<Entregador> entregadores) {
        return new SistemaAlocacao(entregadores, new CriterioOrdenacaoPedidos(), new MenorIdDisponivel());
    }

    public static void main(String[] args) {

        // ---------- TC01 ----------
        {
            Entregador e1 = new Entregador(1, 10, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1));
            ResultadoAlocacao r = sistema.alocar(Arrays.asList(new Pedido(1, 5, 2, 3)));
            checar("TC01", idsIguais(e1.getRota(), 1) && e1.getCargaAtual() == 5 && r.getPendentes().isEmpty(),
                    "alocacao simples, um pedido cabe em um entregador");
        }

        // ---------- TC02 ----------
        {
            Entregador e1 = new Entregador(1, 20, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1));
            sistema.alocar(Arrays.asList(new Pedido(1, 5, 1, 5), new Pedido(2, 5, 3, 10)));
            checar("TC02", idsIguais(e1.getRota(), 2, 1) && e1.getCargaAtual() == 10,
                    "prioridade mais alta atendida primeiro, mesmo mais distante");
        }

        // ---------- TC03 ----------
        {
            Entregador e1 = new Entregador(1, 20, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1));
            sistema.alocar(Arrays.asList(new Pedido(1, 4, 2, 8), new Pedido(2, 4, 2, 3)));
            checar("TC03", idsIguais(e1.getRota(), 2, 1) && e1.getCargaAtual() == 8,
                    "mesma prioridade: desempate pela menor distancia");
        }

        // ---------- TC04 ----------
        {
            Entregador e1 = new Entregador(1, 5, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1));
            ResultadoAlocacao r = sistema.alocar(Arrays.asList(new Pedido(1, 8, 2, 1)));
            checar("TC04", e1.getRota().isEmpty() && e1.getCargaAtual() == 0 && idsIguais(r.getPendentes(), 1),
                    "pedido excede a capacidade do unico entregador");
        }

        // ---------- TC05 ----------
        {
            Entregador e1 = new Entregador(1, 10, true);
            Entregador e2 = new Entregador(2, 10, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1, e2));
            ResultadoAlocacao r = sistema.alocar(Arrays.asList(new Pedido(1, 6, 2, 1), new Pedido(2, 6, 2, 1)));
            checar("TC05", idsIguais(e1.getRota(), 1) && idsIguais(e2.getRota(), 2) && r.getPendentes().isEmpty(),
                    "pedido que nao cabe mais no primeiro entregador vai para o proximo disponivel");
        }

        // ---------- TC06 ----------
        {
            Entregador e1 = new Entregador(1, 10, false);
            Entregador e2 = new Entregador(2, 10, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1, e2));
            ResultadoAlocacao r = sistema.alocar(Arrays.asList(new Pedido(1, 5, 2, 1)));
            checar("TC06", e1.getRota().isEmpty() && idsIguais(e2.getRota(), 1) && r.getPendentes().isEmpty()
                            && !r.getEntregadoresDisponiveis().contains(e1),
                    "entregador indisponivel e ignorado na alocacao");
        }

        // ---------- TC07 ----------
        {
            Entregador e1 = new Entregador(1, 10, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1));
            ResultadoAlocacao r = sistema.alocar(Arrays.asList(new Pedido(1, 7, 3, 2), new Pedido(2, 5, 1, 1)));
            checar("TC07", idsIguais(e1.getRota(), 1) && e1.getCargaAtual() == 7 && idsIguais(r.getPendentes(), 2),
                    "prioridade alta consome a capacidade, deixando a baixa pendente");
        }

        // ---------- TC08 ----------
        {
            Entregador e1 = new Entregador(1, 8, true);
            Entregador e2 = new Entregador(2, 8, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1, e2));
            ResultadoAlocacao r = sistema.alocar(Arrays.asList(
                    new Pedido(1, 5, 3, 5), new Pedido(2, 5, 3, 2), new Pedido(3, 3, 1, 1)));
            checar("TC08", idsIguais(e1.getRota(), 2, 3) && e1.getCargaAtual() == 8
                            && idsIguais(e2.getRota(), 1) && e2.getCargaAtual() == 5
                            && r.getPendentes().isEmpty(),
                    "combinacao de prioridade, desempate por distancia e distribuicao entre dois entregadores");
        }

        // ---------- TC09 ----------
        {
            Entregador e1 = new Entregador(1, 5, true);
            Entregador e2 = new Entregador(2, 5, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1, e2));
            ResultadoAlocacao r = sistema.alocar(Arrays.asList(new Pedido(1, 6, 3, 1), new Pedido(2, 4, 2, 1)));
            checar("TC09", idsIguais(e1.getRota(), 2) && e1.getCargaAtual() == 4
                            && e2.getRota().isEmpty() && idsIguais(r.getPendentes(), 1),
                    "pedido de maior prioridade fica pendente por exceder capacidade de todos");
        }

        // ---------- TC10 ----------
        {
            Entregador e1 = new Entregador(1, 5, true);
            Entregador e2 = new Entregador(2, 5, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1, e2));
            ResultadoAlocacao r = sistema.alocar(Arrays.asList(new Pedido(1, 5, 2, 1), new Pedido(2, 5, 2, 2)));
            checar("TC10", idsIguais(e1.getRota(), 1) && e1.getCargaAtual() == 5
                            && idsIguais(e2.getRota(), 2) && e2.getCargaAtual() == 5
                            && r.getPendentes().isEmpty(),
                    "dois pedidos preenchem exatamente dois entregadores diferentes");
        }

        // ---------- TC11 ----------
        {
            Entregador e1 = new Entregador(1, 10, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1));
            ResultadoAlocacao r = sistema.alocar(Arrays.asList(new Pedido(1, 6, 2, 1), new Pedido(2, 4, 2, 2)));
            checar("TC11", idsIguais(e1.getRota(), 1, 2) && e1.getCargaAtual() == 10 && r.getPendentes().isEmpty(),
                    "capacidade exata: soma dos pesos alocados igual a capacidade maxima");
        }

        // ---------- TC12 ----------
        {
            Entregador e1 = new Entregador(1, 20, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1));
            ResultadoAlocacao r = sistema.alocar(Arrays.asList(new Pedido(1, 3, 2, 5), new Pedido(2, 3, 2, 5)));
            checar("TC12", idsIguais(e1.getRota(), 1, 2) && r.getPendentes().isEmpty(),
                    "empate total (mesma prioridade e distancia): desempate pelo menor id");
        }

        // ---------- TC13 ----------
        {
            SistemaAlocacao sistema = criarSistema(Arrays.asList());
            ResultadoAlocacao r = sistema.alocar(Arrays.asList());
            checar("TC13", r.getPendentes().isEmpty() && r.getInvalidos().isEmpty(),
                    "entrada totalmente vazia");
        }

        // ---------- TC14 ----------
        {
            Entregador e1 = new Entregador(1, 10, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1));
            ResultadoAlocacao r = sistema.alocar(Arrays.asList(new Pedido(1, 5, 5, 1)));
            checar("TC14", e1.getRota().isEmpty() && r.getPendentes().isEmpty() && idsIguais(r.getInvalidos(), 1),
                    "prioridade invalida (fora de 1..3): pedido rejeitado, nao processado");
        }

        // ---------- TC15 ----------
        {
            Entregador e1 = new Entregador(1, 10, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1));
            ResultadoAlocacao r = sistema.alocar(Arrays.asList(new Pedido(1, -2, 2, 1)));
            checar("TC15", e1.getRota().isEmpty() && r.getPendentes().isEmpty() && idsIguais(r.getInvalidos(), 1),
                    "peso invalido (nao positivo): pedido rejeitado, nao processado");
        }

        System.out.println();
        System.out.println("== Demonstracao de fluxo (TC08) ==");
        {
            Entregador e1 = new Entregador(1, 8, true);
            Entregador e2 = new Entregador(2, 8, true);
            SistemaAlocacao sistema = criarSistema(Arrays.asList(e1, e2));
            ResultadoAlocacao r = sistema.alocar(Arrays.asList(
                    new Pedido(1, 5, 3, 5), new Pedido(2, 5, 3, 2), new Pedido(3, 3, 1, 1)));
            for (Entregador e : r.getEntregadoresDisponiveis()) {
                System.out.println(e + " -> rota = " + e.getRota() + ", carga = " + e.getCargaAtual());
            }
            System.out.println("Pendentes = " + r.getPendentes());
            System.out.println("Invalidos = " + r.getInvalidos());
        }

        System.out.println();
        System.out.println(testesOk + "/" + totalTestes + " casos de teste passaram.");
        if (testesOk != totalTestes) {
            System.exit(1);
        }
    }
}

#include <stdio.h>
#include "alocacao.h"

static int totalTestes = 0;
static int testesOk = 0;

/* Compara duas listas de ids (ordem importa, pois rota é sequência). */
static int listaIgual(int lista[], int n, int esperado[], int nEsperado) {
    if (n != nEsperado) return 0;
    for (int i = 0; i < n; i++) {
        if (lista[i] != esperado[i]) return 0;
    }
    return 1;
}

static void checar(const char *tc, int condicao, const char *descricao) {
    totalTestes++;
    if (condicao) {
        testesOk++;
        printf("[OK ] %s - %s\n", tc, descricao);
    } else {
        printf("[FAI] %s - %s\n", tc, descricao);
    }
}

int main(void) {
    /* ---------- TC01 ---------- */
    {
        Entregador e[] = {{1, 10, 0, 1}};
        Pedido p[] = {{1, 5, 2, 3}};
        ResultadoAlocacao r = alocarEntregas(e, 1, p, 1);
        int esperado[] = {1};
        checar("TC01", listaIgual(r.porEntregador[0].idsRota, r.porEntregador[0].tamRota, esperado, 1)
                        && r.porEntregador[0].cargaTotal == 5 && r.nPendentes == 0,
               "alocacao simples, um pedido cabe em um entregador");
    }

    /* ---------- TC02 ---------- */
    {
        Entregador e[] = {{1, 20, 0, 1}};
        Pedido p[] = {{1, 5, 1, 5}, {2, 5, 3, 10}};
        ResultadoAlocacao r = alocarEntregas(e, 1, p, 2);
        int esperado[] = {2, 1};
        checar("TC02", listaIgual(r.porEntregador[0].idsRota, r.porEntregador[0].tamRota, esperado, 2)
                        && r.porEntregador[0].cargaTotal == 10,
               "prioridade mais alta atendida primeiro, mesmo mais distante");
    }

    /* ---------- TC03 ---------- */
    {
        Entregador e[] = {{1, 20, 0, 1}};
        Pedido p[] = {{1, 4, 2, 8}, {2, 4, 2, 3}};
        ResultadoAlocacao r = alocarEntregas(e, 1, p, 2);
        int esperado[] = {2, 1};
        checar("TC03", listaIgual(r.porEntregador[0].idsRota, r.porEntregador[0].tamRota, esperado, 2)
                        && r.porEntregador[0].cargaTotal == 8,
               "mesma prioridade: desempate pela menor distancia");
    }

    /* ---------- TC04 ---------- */
    {
        Entregador e[] = {{1, 5, 0, 1}};
        Pedido p[] = {{1, 8, 2, 1}};
        ResultadoAlocacao r = alocarEntregas(e, 1, p, 1);
        int esperadoPend[] = {1};
        checar("TC04", r.porEntregador[0].tamRota == 0 && r.porEntregador[0].cargaTotal == 0
                        && listaIgual(r.pendentes, r.nPendentes, esperadoPend, 1),
               "pedido excede a capacidade do unico entregador");
    }

    /* ---------- TC05 ---------- */
    {
        Entregador e[] = {{1, 10, 0, 1}, {2, 10, 0, 1}};
        Pedido p[] = {{1, 6, 2, 1}, {2, 6, 2, 1}};
        ResultadoAlocacao r = alocarEntregas(e, 2, p, 2);
        int esp1[] = {1}, esp2[] = {2};
        checar("TC05", listaIgual(r.porEntregador[0].idsRota, r.porEntregador[0].tamRota, esp1, 1)
                        && listaIgual(r.porEntregador[1].idsRota, r.porEntregador[1].tamRota, esp2, 1)
                        && r.nPendentes == 0,
               "pedido que nao cabe mais no primeiro entregador vai para o proximo disponivel");
    }

    /* ---------- TC06 ---------- */
    {
        Entregador e[] = {{1, 10, 0, 0}, {2, 10, 0, 1}};
        Pedido p[] = {{1, 5, 2, 1}};
        ResultadoAlocacao r = alocarEntregas(e, 2, p, 1);
        int esp2[] = {1};
        checar("TC06", r.porEntregador[0].tamRota == 0
                        && listaIgual(r.porEntregador[1].idsRota, r.porEntregador[1].tamRota, esp2, 1)
                        && r.nPendentes == 0,
               "entregador indisponivel e ignorado na alocacao");
    }

    /* ---------- TC07 ---------- */
    {
        Entregador e[] = {{1, 10, 0, 1}};
        Pedido p[] = {{1, 7, 3, 2}, {2, 5, 1, 1}};
        ResultadoAlocacao r = alocarEntregas(e, 1, p, 2);
        int esp[] = {1}, espPend[] = {2};
        checar("TC07", listaIgual(r.porEntregador[0].idsRota, r.porEntregador[0].tamRota, esp, 1)
                        && r.porEntregador[0].cargaTotal == 7
                        && listaIgual(r.pendentes, r.nPendentes, espPend, 1),
               "prioridade alta consome a capacidade, deixando a baixa pendente");
    }

    /* ---------- TC08 ---------- */
    {
        Entregador e[] = {{1, 8, 0, 1}, {2, 8, 0, 1}};
        Pedido p[] = {{1, 5, 3, 5}, {2, 5, 3, 2}, {3, 3, 1, 1}};
        ResultadoAlocacao r = alocarEntregas(e, 2, p, 3);
        int esp1[] = {2, 3}, esp2[] = {1};
        checar("TC08", listaIgual(r.porEntregador[0].idsRota, r.porEntregador[0].tamRota, esp1, 2)
                        && r.porEntregador[0].cargaTotal == 8
                        && listaIgual(r.porEntregador[1].idsRota, r.porEntregador[1].tamRota, esp2, 1)
                        && r.porEntregador[1].cargaTotal == 5
                        && r.nPendentes == 0,
               "combinacao de prioridade, desempate por distancia e distribuicao entre dois entregadores");
    }

    /* ---------- TC09 ---------- */
    {
        Entregador e[] = {{1, 5, 0, 1}, {2, 5, 0, 1}};
        Pedido p[] = {{1, 6, 3, 1}, {2, 4, 2, 1}};
        ResultadoAlocacao r = alocarEntregas(e, 2, p, 2);
        int esp1[] = {2}, espPend[] = {1};
        checar("TC09", listaIgual(r.porEntregador[0].idsRota, r.porEntregador[0].tamRota, esp1, 1)
                        && r.porEntregador[0].cargaTotal == 4
                        && r.porEntregador[1].tamRota == 0
                        && listaIgual(r.pendentes, r.nPendentes, espPend, 1),
               "pedido de maior prioridade fica pendente por exceder capacidade de todos");
    }

    /* ---------- TC10 ---------- */
    {
        Entregador e[] = {{1, 5, 0, 1}, {2, 5, 0, 1}};
        Pedido p[] = {{1, 5, 2, 1}, {2, 5, 2, 2}};
        ResultadoAlocacao r = alocarEntregas(e, 2, p, 2);
        int esp1[] = {1}, esp2[] = {2};
        checar("TC10", listaIgual(r.porEntregador[0].idsRota, r.porEntregador[0].tamRota, esp1, 1)
                        && r.porEntregador[0].cargaTotal == 5
                        && listaIgual(r.porEntregador[1].idsRota, r.porEntregador[1].tamRota, esp2, 1)
                        && r.porEntregador[1].cargaTotal == 5
                        && r.nPendentes == 0,
               "dois pedidos preenchem exatamente dois entregadores diferentes");
    }

    /* ---------- TC11 ---------- */
    {
        Entregador e[] = {{1, 10, 0, 1}};
        Pedido p[] = {{1, 6, 2, 1}, {2, 4, 2, 2}};
        ResultadoAlocacao r = alocarEntregas(e, 1, p, 2);
        int esp[] = {1, 2};
        checar("TC11", listaIgual(r.porEntregador[0].idsRota, r.porEntregador[0].tamRota, esp, 2)
                        && r.porEntregador[0].cargaTotal == 10 && r.nPendentes == 0,
               "capacidade exata: soma dos pesos alocados igual a capacidade maxima");
    }

    /* ---------- TC12 ---------- */
    {
        Entregador e[] = {{1, 20, 0, 1}};
        Pedido p[] = {{1, 3, 2, 5}, {2, 3, 2, 5}};
        ResultadoAlocacao r = alocarEntregas(e, 1, p, 2);
        int esp[] = {1, 2};
        checar("TC12", listaIgual(r.porEntregador[0].idsRota, r.porEntregador[0].tamRota, esp, 2)
                        && r.nPendentes == 0,
               "empate total (mesma prioridade e distancia): desempate pelo menor id");
    }

    /* ---------- TC13 ---------- */
    {
        Entregador e[1];
        Pedido p[1];
        ResultadoAlocacao r = alocarEntregas(e, 0, p, 0);
        checar("TC13", r.nPendentes == 0 && r.nInvalidos == 0,
               "entrada totalmente vazia");
    }

    /* ---------- TC14 ---------- */
    {
        Entregador e[] = {{1, 10, 0, 1}};
        Pedido p[] = {{1, 5, 5, 1}};
        ResultadoAlocacao r = alocarEntregas(e, 1, p, 1);
        int espInv[] = {1};
        checar("TC14", r.porEntregador[0].tamRota == 0 && r.nPendentes == 0
                        && listaIgual(r.invalidos, r.nInvalidos, espInv, 1),
               "prioridade invalida (fora de 1..3): pedido rejeitado, nao processado");
    }

    /* ---------- TC15 ---------- */
    {
        Entregador e[] = {{1, 10, 0, 1}};
        Pedido p[] = {{1, -2, 2, 1}};
        ResultadoAlocacao r = alocarEntregas(e, 1, p, 1);
        int espInv[] = {1};
        checar("TC15", r.porEntregador[0].tamRota == 0 && r.nPendentes == 0
                        && listaIgual(r.invalidos, r.nInvalidos, espInv, 1),
               "peso invalido (nao positivo): pedido rejeitado, nao processado");
    }

    printf("\n== Demonstracao de fluxo (TC08) ==\n");
    {
        Entregador e[] = {{1, 8, 0, 1}, {2, 8, 0, 1}};
        Pedido p[] = {{1, 5, 3, 5}, {2, 5, 3, 2}, {3, 3, 1, 1}};
        ResultadoAlocacao r = alocarEntregas(e, 2, p, 3);
        imprimirResultado(e, 2, r);
    }

    printf("\n%d/%d casos de teste passaram.\n", testesOk, totalTestes);
    return (testesOk == totalTestes) ? 0 : 1;
}

#include <stdio.h>
#include <stdlib.h>
#include "alocacao.h"

/* Subprograma 1: valida um pedido isoladamente (sem depender de
 * nenhum estado externo). Regra do contrato: peso > 0 e
 * prioridade em {1,2,3}. */
int pedidoValido(Pedido p) {
    return (p.peso > 0) && (p.prioridade >= 1 && p.prioridade <= 3);
}

/* Comparador usado pelo qsort: ordena por prioridade decrescente,
 * depois distancia crescente, depois id crescente. */
static int comparaPedidos(const void *a, const void *b) {
    const Pedido *pa = (const Pedido *)a;
    const Pedido *pb = (const Pedido *)b;

    if (pa->prioridade != pb->prioridade) {
        return pb->prioridade - pa->prioridade;
    }
    if (pa->distancia != pb->distancia) {
        return (pa->distancia < pb->distancia) ? -1 : 1;
    }
    return pa->id - pb->id;
}

/* Subprograma 2: ordena o vetor de pedidos IN-PLACE (efeito
 * colateral deliberado sobre a copia local de pedidos validos
 * usada em alocarEntregas). */
void ordenarPedidos(Pedido pedidos[], int n) {
    qsort(pedidos, (size_t)n, sizeof(Pedido), comparaPedidos);
}

/* Subprograma 3: busca, entre os entregadores DISPONIVEIS com
 * capacidade restante suficiente, o de MENOR id. Retorna o indice
 * no array recebido, ou -1 se nenhum servir. */
int indiceEntregadorDisponivel(Entregador entregadores[], int nEntregadores, double pesoNecessario) {
    int melhorIndice = -1;
    for (int i = 0; i < nEntregadores; i++) {
        if (!entregadores[i].disponivel) {
            continue;
        }
        double restante = entregadores[i].capacidade - entregadores[i].capacidadeUsada;
        if (restante >= pesoNecessario) {
            if (melhorIndice == -1 || entregadores[i].id < entregadores[melhorIndice].id) {
                melhorIndice = i;
            }
        }
    }
    return melhorIndice;
}

/* Subprograma principal: orquestra validacao -> ordenacao ->
 * alocacao guloso, mutando o estado dos entregadores
 * (capacidadeUsada) a cada pedido atribuido. */
ResultadoAlocacao alocarEntregas(Entregador entregadores[], int nEntregadores,
                                  Pedido pedidosOriginais[], int nPedidos) {
    ResultadoAlocacao resultado;
    resultado.nPendentes = 0;
    resultado.nInvalidos = 0;
    for (int i = 0; i < nEntregadores; i++) {
        resultado.porEntregador[i].tamRota = 0;
        resultado.porEntregador[i].cargaTotal = 0.0;
    }

    /* Reinicia o estado mutavel dos entregadores a cada execucao:
     * capacidadeUsada representa apenas o que foi ocupado NESTA
     * chamada, nao um acumulo entre chamadas diferentes. */
    for (int i = 0; i < nEntregadores; i++) {
        entregadores[i].capacidadeUsada = 0.0;
    }

    /* Separa pedidos validos de invalidos, sem alterar o array
     * original recebido pelo chamador. */
    Pedido validos[MAX_PEDIDOS];
    int nValidos = 0;
    for (int i = 0; i < nPedidos; i++) {
        if (pedidoValido(pedidosOriginais[i])) {
            validos[nValidos++] = pedidosOriginais[i];
        } else {
            resultado.invalidos[resultado.nInvalidos++] = pedidosOriginais[i].id;
        }
    }

    ordenarPedidos(validos, nValidos);

    for (int i = 0; i < nValidos; i++) {
        Pedido p = validos[i];
        int idx = indiceEntregadorDisponivel(entregadores, nEntregadores, p.peso);
        if (idx == -1) {
            resultado.pendentes[resultado.nPendentes++] = p.id;
        } else {
            /* EFEITO COLATERAL: muta o estado do entregador escolhido. */
            entregadores[idx].capacidadeUsada += p.peso;

            ResultadoEntregador *re = &resultado.porEntregador[idx];
            re->idsRota[re->tamRota++] = p.id;
            re->cargaTotal += p.peso;
        }
    }

    return resultado;
}

/* Subprograma auxiliar de apoio: só imprime o resultado, para
 * demonstrar o fluxo de execucao. Nao participa da logica de
 * decisao. */
void imprimirResultado(Entregador entregadores[], int nEntregadores, ResultadoAlocacao r) {
    for (int i = 0; i < nEntregadores; i++) {
        if (!entregadores[i].disponivel) {
            continue;
        }
        printf("Entregador %d -> rota = [", entregadores[i].id);
        for (int j = 0; j < r.porEntregador[i].tamRota; j++) {
            printf("%d%s", r.porEntregador[i].idsRota[j],
                   (j < r.porEntregador[i].tamRota - 1) ? "," : "");
        }
        printf("], carga = %.1f\n", r.porEntregador[i].cargaTotal);
    }

    printf("Pendentes = [");
    for (int i = 0; i < r.nPendentes; i++) {
        printf("%d%s", r.pendentes[i], (i < r.nPendentes - 1) ? "," : "");
    }
    printf("]\n");

    printf("Invalidos = [");
    for (int i = 0; i < r.nInvalidos; i++) {
        printf("%d%s", r.invalidos[i], (i < r.nInvalidos - 1) ? "," : "");
    }
    printf("]\n");
}

#ifndef ALOCACAO_H
#define ALOCACAO_H

#define MAX_ENTREGADORES 20
#define MAX_PEDIDOS 50

/* Entregador: capacidade é fixa; capacidadeUsada é ESTADO MUTÁVEL,
 * reiniciado a cada chamada de alocarEntregas(). */
typedef struct {
    int id;
    double capacidade;
    double capacidadeUsada;
    int disponivel; /* 1 = disponivel, 0 = indisponivel */
} Entregador;

typedef struct {
    int id;
    double peso;
    int prioridade;  /* esperado 1..3, mas pode vir invalido */
    double distancia;
} Pedido;

/* Rota de um entregador: lista de ids de pedidos na ordem em que
 * foram atribuidos, e a carga total acumulada. */
typedef struct {
    int idsRota[MAX_PEDIDOS];
    int tamRota;
    double cargaTotal;
} ResultadoEntregador;

/* Resultado agregado de uma execucao de alocarEntregas.
 * porEntregador[i] corresponde ao entregador de mesmo indice
 * no array de entregadores recebido. */
typedef struct {
    ResultadoEntregador porEntregador[MAX_ENTREGADORES];
    int pendentes[MAX_PEDIDOS];
    int nPendentes;
    int invalidos[MAX_PEDIDOS];
    int nInvalidos;
} ResultadoAlocacao;

int pedidoValido(Pedido p);
void ordenarPedidos(Pedido pedidos[], int n);
int indiceEntregadorDisponivel(Entregador entregadores[], int nEntregadores, double pesoNecessario);
ResultadoAlocacao alocarEntregas(Entregador entregadores[], int nEntregadores,
                                  Pedido pedidosOriginais[], int nPedidos);
void imprimirResultado(Entregador entregadores[], int nEntregadores, ResultadoAlocacao r);

#endif

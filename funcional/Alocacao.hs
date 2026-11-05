-- | Núcleo PURO da alocação de entregas (Etapa 06 - paradigma funcional).
--
-- Nenhuma função deste módulo faz entrada/saída nem altera nada:
-- todas recebem valores e devolvem NOVOS valores (imutabilidade).
module Alocacao
  ( Entregador (..)
  , Pedido (..)
  , Rota (..)
  , Resultado (..)
  , pedidoValido
  , compararPedidos
  , cargaRota
  , podeReceber
  , escolherRota
  , atribuir
  , substituirRota
  , distribuir
  , alocar
  ) where

import Data.List (minimumBy, partition, sortBy)
import Data.Ord (Down (..), comparing)

-- ---------------------------------------------------------------
-- Tipos de dados (todos imutáveis: não existe "setar" um campo)
-- ---------------------------------------------------------------

data Entregador = Entregador
  { entId         :: Int
  , entCapacidade :: Double
  , entDisponivel :: Bool
  } deriving (Show, Eq)

data Pedido = Pedido
  { pedId         :: Int
  , pedPeso       :: Double
  , pedPrioridade :: Int
  , pedDistancia  :: Double
  } deriving (Show, Eq)

-- | Uma rota é o entregador + os pedidos atribuídos a ele, em ordem.
-- Não existe "capacidadeUsada" guardada: ela é CALCULADA a partir da rota.
data Rota = Rota
  { rotaEntregador :: Entregador
  , rotaPedidos    :: [Pedido]
  } deriving (Show, Eq)

data Resultado = Resultado
  { resRotas      :: [Rota]
  , resPendentes  :: [Pedido]
  , resInvalidos  :: [Pedido]
  } deriving (Show, Eq)

-- ---------------------------------------------------------------
-- Funções puras
-- ---------------------------------------------------------------

-- | Regra do contrato: peso > 0 e prioridade em {1,2,3}.
pedidoValido :: Pedido -> Bool
pedidoValido p = pedPeso p > 0 && pedPrioridade p `elem` [1, 2, 3]

-- | Critério de ordenação do contrato, construído por COMPOSIÇÃO de
-- comparadores: prioridade decrescente, depois distância crescente,
-- depois id crescente. O operador (<>) combina os três: só olha o
-- próximo critério se o anterior empatar.
compararPedidos :: Pedido -> Pedido -> Ordering
compararPedidos =
  comparing (Down . pedPrioridade)
    <> comparing pedDistancia
    <> comparing pedId

-- | Carga de uma rota: calculada (não armazenada).
cargaRota :: Rota -> Double
cargaRota = sum . map pedPeso . rotaPedidos

-- | O entregador está disponível e o pedido cabe no que sobra?
podeReceber :: Pedido -> Rota -> Bool
podeReceber p r =
  entDisponivel e && entCapacidade e - cargaRota r >= pedPeso p
  where
    e = rotaEntregador r

-- | Entre as rotas que comportam o pedido, escolhe a de menor id.
-- Devolve Maybe: Nothing no lugar do "-1" do C ou da exceção do Java.
escolherRota :: [Rota] -> Pedido -> Maybe Rota
escolherRota rotas p =
  case filter (podeReceber p) rotas of
    []         -> Nothing
    candidatas -> Just (minimumBy (comparing (entId . rotaEntregador)) candidatas)

-- | Devolve uma NOVA rota com o pedido no final (a original não muda).
atribuir :: Pedido -> Rota -> Rota
atribuir p r = r { rotaPedidos = rotaPedidos r ++ [p] }

-- | Devolve uma NOVA lista de rotas, trocando a rota do mesmo entregador.
substituirRota :: Rota -> [Rota] -> [Rota]
substituirRota nova = map troca
  where
    troca r
      | entId (rotaEntregador r) == entId (rotaEntregador nova) = nova
      | otherwise                                                = r

-- | Distribuição por RECURSÃO (de cauda, com acumulador de pendentes).
-- Caso base: não há mais pedidos. Caso recursivo: trata o primeiro
-- pedido e chama a si mesma com o resto da lista.
distribuir :: [Rota] -> [Pedido] -> [Pedido] -> ([Rota], [Pedido])
distribuir rotas pendentes [] = (rotas, reverse pendentes)
distribuir rotas pendentes (p : ps) =
  case escolherRota rotas p of
    Just r  -> distribuir (substituirRota (atribuir p r) rotas) pendentes ps
    Nothing -> distribuir rotas (p : pendentes) ps

-- | Função principal: uma PIPELINE de transformações sobre os dados.
-- validar/separar -> ordenar -> distribuir. Nada é modificado;
-- cada etapa produz um valor novo a partir do anterior.
alocar :: [Entregador] -> [Pedido] -> Resultado
alocar entregadores pedidos =
  Resultado rotasFinais pendentes invalidos
  where
    (validos, invalidos)     = partition pedidoValido pedidos
    ordenados                = sortBy compararPedidos validos
    rotasIniciais            = [Rota e [] | e <- entregadores, entDisponivel e]
    (rotasFinais, pendentes) = distribuir rotasIniciais [] ordenados

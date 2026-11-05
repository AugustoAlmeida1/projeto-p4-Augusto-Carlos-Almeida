-- | Execução dos 15 casos de teste da Etapa 02 sobre a implementação funcional.
--
-- Este é o ÚNICO ponto do programa que interage com o mundo externo
-- (imprimir na tela e devolver o código de saída). Os testes em si
-- são DADOS: uma lista de (id, descrição, passou?) calculada por
-- funções puras. A função main só pega essa lista e a imprime.
module Main (main) where

import Alocacao
import System.Exit (exitFailure, exitSuccess)

-- ---------------------------------------------------------------
-- Construtores curtos, só para deixar os casos legíveis
-- ---------------------------------------------------------------

ent :: Int -> Double -> Bool -> Entregador
ent = Entregador

ped :: Int -> Double -> Int -> Double -> Pedido
ped = Pedido

-- ---------------------------------------------------------------
-- Consultas puras sobre o Resultado
-- ---------------------------------------------------------------

idsRota :: Int -> Resultado -> [Int]
idsRota i r =
  case [map pedId (rotaPedidos x) | x <- resRotas r, entId (rotaEntregador x) == i] of
    (ids : _) -> ids
    []        -> []

cargaDe :: Int -> Resultado -> Double
cargaDe i r = sum [cargaRota x | x <- resRotas r, entId (rotaEntregador x) == i]

aparece :: Int -> Resultado -> Bool
aparece i r = i `elem` map (entId . rotaEntregador) (resRotas r)

pendentes :: Resultado -> [Int]
pendentes = map pedId . resPendentes

invalidos :: Resultado -> [Int]
invalidos = map pedId . resInvalidos

-- ---------------------------------------------------------------
-- Casos de teste (dados puros)
-- ---------------------------------------------------------------

caso :: String -> String -> Resultado -> (Resultado -> Bool) -> (String, String, Bool)
caso nome descricao resultado condicao = (nome, descricao, condicao resultado)

casos :: [(String, String, Bool)]
casos =
  [ caso "TC01" "alocacao simples, um pedido cabe em um entregador"
      (alocar [ent 1 10 True] [ped 1 5 2 3])
      (\r -> idsRota 1 r == [1] && cargaDe 1 r == 5 && null (pendentes r))

  , caso "TC02" "prioridade mais alta atendida primeiro, mesmo mais distante"
      (alocar [ent 1 20 True] [ped 1 5 1 5, ped 2 5 3 10])
      (\r -> idsRota 1 r == [2, 1] && cargaDe 1 r == 10)

  , caso "TC03" "mesma prioridade: desempate pela menor distancia"
      (alocar [ent 1 20 True] [ped 1 4 2 8, ped 2 4 2 3])
      (\r -> idsRota 1 r == [2, 1] && cargaDe 1 r == 8)

  , caso "TC04" "pedido excede a capacidade do unico entregador"
      (alocar [ent 1 5 True] [ped 1 8 2 1])
      (\r -> null (idsRota 1 r) && cargaDe 1 r == 0 && pendentes r == [1])

  , caso "TC05" "pedido que nao cabe mais no primeiro entregador vai para o proximo disponivel"
      (alocar [ent 1 10 True, ent 2 10 True] [ped 1 6 2 1, ped 2 6 2 1])
      (\r -> idsRota 1 r == [1] && idsRota 2 r == [2] && null (pendentes r))

  , caso "TC06" "entregador indisponivel e ignorado na alocacao"
      (alocar [ent 1 10 False, ent 2 10 True] [ped 1 5 2 1])
      (\r -> null (idsRota 1 r) && idsRota 2 r == [1] && null (pendentes r) && not (aparece 1 r))

  , caso "TC07" "prioridade alta consome a capacidade, deixando a baixa pendente"
      (alocar [ent 1 10 True] [ped 1 7 3 2, ped 2 5 1 1])
      (\r -> idsRota 1 r == [1] && cargaDe 1 r == 7 && pendentes r == [2])

  , caso "TC08" "combinacao de prioridade, desempate por distancia e distribuicao entre dois entregadores"
      (alocar [ent 1 8 True, ent 2 8 True] [ped 1 5 3 5, ped 2 5 3 2, ped 3 3 1 1])
      (\r -> idsRota 1 r == [2, 3] && cargaDe 1 r == 8
             && idsRota 2 r == [1] && cargaDe 2 r == 5
             && null (pendentes r))

  , caso "TC09" "pedido de maior prioridade fica pendente por exceder capacidade de todos"
      (alocar [ent 1 5 True, ent 2 5 True] [ped 1 6 3 1, ped 2 4 2 1])
      (\r -> idsRota 1 r == [2] && cargaDe 1 r == 4 && null (idsRota 2 r) && pendentes r == [1])

  , caso "TC10" "dois pedidos preenchem exatamente dois entregadores diferentes"
      (alocar [ent 1 5 True, ent 2 5 True] [ped 1 5 2 1, ped 2 5 2 2])
      (\r -> idsRota 1 r == [1] && cargaDe 1 r == 5
             && idsRota 2 r == [2] && cargaDe 2 r == 5
             && null (pendentes r))

  , caso "TC11" "capacidade exata: soma dos pesos alocados igual a capacidade maxima"
      (alocar [ent 1 10 True] [ped 1 6 2 1, ped 2 4 2 2])
      (\r -> idsRota 1 r == [1, 2] && cargaDe 1 r == 10 && null (pendentes r))

  , caso "TC12" "empate total (mesma prioridade e distancia): desempate pelo menor id"
      (alocar [ent 1 20 True] [ped 1 3 2 5, ped 2 3 2 5])
      (\r -> idsRota 1 r == [1, 2] && null (pendentes r))

  , caso "TC13" "entrada totalmente vazia"
      (alocar [] [])
      (\r -> null (resRotas r) && null (pendentes r) && null (invalidos r))

  , caso "TC14" "prioridade invalida (fora de 1..3): pedido rejeitado, nao processado"
      (alocar [ent 1 10 True] [ped 1 5 5 1])
      (\r -> null (idsRota 1 r) && null (pendentes r) && invalidos r == [1])

  , caso "TC15" "peso invalido (nao positivo): pedido rejeitado, nao processado"
      (alocar [ent 1 10 True] [ped 1 (-2) 2 1])
      (\r -> null (idsRota 1 r) && null (pendentes r) && invalidos r == [1])
  ]

-- ---------------------------------------------------------------
-- Formatação (pura): transforma dados em texto, sem imprimir nada
-- ---------------------------------------------------------------

formatarCaso :: (String, String, Bool) -> String
formatarCaso (nome, descricao, ok) =
  (if ok then "[OK ] " else "[FAI] ") ++ nome ++ " - " ++ descricao

formatarResultado :: Resultado -> String
formatarResultado r =
  unlines (map linhaRota (resRotas r)
           ++ ["Pendentes = " ++ show (pendentes r), "Invalidos = " ++ show (invalidos r)])
  where
    linhaRota x =
      "Entregador " ++ show (entId (rotaEntregador x))
        ++ " -> rota = " ++ show (map pedId (rotaPedidos x))
        ++ ", carga = " ++ show (cargaRota x)

-- ---------------------------------------------------------------
-- Único ponto IMPURO do programa (entrada/saída)
-- ---------------------------------------------------------------

main :: IO ()
main = do
  mapM_ (putStrLn . formatarCaso) casos
  putStrLn ""
  putStrLn "== Demonstracao de fluxo (TC08) =="
  putStr (formatarResultado demo)
  putStrLn ""
  putStrLn (show passaram ++ "/" ++ show total ++ " casos de teste passaram.")
  if passaram == total then exitSuccess else exitFailure
  where
    total    = length casos
    passaram = length (filter (\(_, _, ok) -> ok) casos)
    demo     = alocar [ent 1 8 True, ent 2 8 True] [ped 1 5 3 5, ped 2 5 3 2, ped 3 3 1 1]

# prioridade

Filas de prioridade do zero em Java 21: monte binário, monte pareado e monte
esquerdista, com as comparações e as trocas contadas uma a uma.

O juiz é a **fila por lista ordenada**, que é a definição escrita como código. Ela
guarda os valores numa lista e mantém a lista ordenada; tirar o menor é tirar o
primeiro. Não há propriedade de monte, não há vetor, não há distância até o vazio.

Ela custa n por inserção e por isso não serve para nada a não ser julgar. E é
justamente por ser burra que ela julga: se os três montes estivessem errados do
mesmo jeito, ela não estaria errada junto.

## As três montagens

```
$ mvn exec:java -Dexec.args="montagem"

  vetor                       tamanho     de baixo    inserindo    de cima       comp. de baixo        comp. inserindo     comp. por elemento
  crescente                      1000         vale         vale       vale                  999                    999                   1.00
  decrescente                    1000         vale         vale        NAO                 1982                   7987                   1.98
  todos iguais                   1000         vale         vale       vale                  999                    999                   1.00
  sorteado                       1000         vale         vale        NAO                 1850                   2248                   1.85
  serrote                        1000         vale         vale        NAO                 1735                   1778                   1.74
  quase crescente                1000         vale         vale        NAO                 1041                   1030                   1.04
```

Montar de baixo é a montagem de Floyd, de 1964: põe os valores no vetor de qualquer
jeito e desce cada nó, **do último que tem filho até a raiz**.

Montar de cima faz exatamente a mesma coisa na ordem inversa, e é a versão
plausível. A ordem é que importa: descer um nó exige que as subárvores dele já sejam
montes, e indo de cima para baixo elas ainda não são.

```
       tamanho         de baixo          inserindo    de baixo por elemento          razao
           100              177                202                     1.77           1.14
          1000             1890               2291                     1.89           1.21
         10000            18859              22747                     1.89           1.21
        100000           187919             227337                     1.88           1.21
       1000000          1881388            2281204                     1.88           1.21
```

## O que o emparelhamento faz

```
$ mvn exec:java -Dexec.args="chave"

     tamanho   profundidade certa   profundidade plausivel     grau certa   grau plausivel    comparacoes certa   comparacoes plausivel      razao
         100                    6                        7              9               44                  609                    1835        3.0
         500                    8                        8             12              203                 4369                   46418       10.6
        1000                    9                        9             14              368                 9945                  186485       18.8
        2000                   12                        9             17              716                22055                  769638       34.9
        4000                   12                        9             20             1412                49012                 2997040       61.1
```

Tirar a raiz de um monte pareado deixa uma lista de filhos soltos, e juntá-los de
volta é a única decisão do algoritmo. Juntá-los **dois a dois** distribui os filhos
em vários níveis; juntá-los um a um da esquerda para a direita pendura todos no
mesmo nó.

## O caminho da direita

```
     tamanho   caminho certo   caminho plausivel   teto do teorema   propriedade vale   comparacoes certa   comparacoes plausivel
         100               6                 100                 7                sim                 480                    4950
         500               8                 500                 9                sim                3498                  124750
        2000              10                2000                11                sim               17964                 1999000
       10000              13               10000                14                sim              113631                49995000
```

O monte esquerdista tem uma regra além da propriedade do monte: a distância até o
vazio mais próximo do filho da esquerda é sempre maior ou igual à do filho da
direita. A consequência é que o caminho mais à direita nunca passa de log n mais um,
e toda junção acontece ao longo desse caminho.

Tirar a troca de lados mantém a resposta certa e transforma o caminho numa lista do
tamanho da fila.

## Como rodar

```
mvn test
mvn exec:java -Dexec.args="tudo"
```

Os comandos do medidor são `definicao`, `erro`, `montagem`, `custo`, `chave` e
`tudo`.

## O que as medidas me corrigiram

**Das três versões plausíveis, só uma dá resposta errada.** Eu montei três esperando
que as três quebrassem contra o juiz. Uma quebra em 200 de 200 vetores sorteados; as
outras duas estão sempre certas e só custam muito mais.

```
$ mvn exec:java -Dexec.args="erro"

  familia                          vetores        binario   binario so a esquerda        pareado   pareado em fila    esquerdista   esquerdista sem trocar
  sorteados de 30 ate 1000000          200              0                     200              0                 0              0                        0
  sorteados de 200 ate 1000000         200              0                     200              0                 0              0                        0
```

É o tipo de defeito que nenhum teste de corretude pega, porque não há nada de errado
na resposta. O monte esquerdista sem a troca de lados devolve a entrada ordenada,
sempre, gastando 440 vezes mais comparações. Só a contagem de operações vê.

Essa é a razão de o repositório contar comparações e trocas em vez de só comparar
saídas.

**A versão errada do emparelhamento fica mais rasa, não mais funda.** Eu tinha
escrito na documentação da classe que juntar os filhos um a um deixa uma lista
encadeada. A medida mostra o contrário: a profundidade dela é 9 onde a certa é 12, e
ela custa 61 vezes mais.

O que explode é o **grau**: 1.412 filhos no nó mais carregado contra 20. Juntar da
esquerda para a direita pendura todo mundo no mesmo nó, e a próxima retirada tem de
emparelhar essa multidão de novo. A coluna do grau só entrou na tabela depois que a
da profundidade não explicou nada.

**Montar inserindo custa 21% a mais que montar de baixo, e não log n vezes mais.** A
montagem de Floyd é famosa por ser linear onde a óbvia é n log n, e eu esperava ver
essa distância na tabela.

Ela existe no **pior caso**: no vetor decrescente a razão é 4,0. Num vetor sorteado
é 1,21, e não cresce com o tamanho — de 100 a um milhão de elementos ela fica parada
ali.

O motivo é que quase toda inserção para depois de uma ou duas comparações: um valor
novo raramente é menor que o caminho inteiro até a raiz. O log n está lá e quase
nunca é pago.

**Inserir no monte pareado é exatamente uma comparação, sempre.**

```
$ mvn exec:java -Dexec.args="custo"

       tamanho     so inserindo b     so inserindo p     so inserindo e        com tirar b        com tirar p        com tirar e
          1000               2313                999               4709              17266              15174              13657
         10000              22677               9999              72618             239297             224632             203276
        100000             228493              99999             862061            3060454            2972536            2588155
```

A coluna do meio é n menos um em todas as linhas. É isso que torna o monte pareado a
escolha certa em carga que insere muito e tira pouco, que é exatamente o caso de
Dijkstra e de A estrela.

E o monte pareado faz **zero trocas** em todas as linhas da tabela de custo, porque
ele mexe em ponteiro e não em vetor. Somar comparações e trocas num número só
esconderia justamente isso.

## Licença

MIT.

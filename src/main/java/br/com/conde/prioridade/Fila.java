package br.com.conde.prioridade;

/**
 * Uma FILA DE PRIORIDADE: guarda numeros e devolve sempre o MENOR.
 *
 * <p>So isso. As tres implementacoes deste repositorio respondem sempre a mesma
 * coisa e gastam quantidades muito diferentes de comparacoes e de trocas, e e essa
 * diferenca que as tabelas medem.
 *
 * <p>A fila guarda {@code long} e nao um tipo qualquer de proposito. Com
 * {@code Comparable} toda comparacao vira uma chamada virtual e o custo medido
 * passaria a incluir o despacho dinamico, que nao e o assunto. Com {@code long} a
 * comparacao e a conta, e o contador conta a conta.
 */
public interface Fila {

    String nome();

    /** Poe um valor na fila. */
    void inserir(long valor);

    /** Olha o menor sem tirar. Lanca se a fila estiver vazia. */
    long menor();

    /** Tira e devolve o menor. Lanca se a fila estiver vazia. */
    long tirarMenor();

    int tamanho();

    default boolean vazia() {
        return tamanho() == 0;
    }

    /** O contador desta fila, com as comparacoes e trocas acumuladas. */
    Contador contador();

    /** Esvazia a fila e zera o contador. */
    void limpar();

    /**
     * Tira todos os elementos, do menor para o maior.
     *
     * <p>E a conferencia mais forte que existe para uma fila de prioridade: se a
     * sequencia de saida estiver ordenada e tiver exatamente os mesmos valores da
     * entrada, a fila esta certa. Qualquer erro na propriedade do monte aparece aqui
     * como um numero fora de lugar.
     */
    default long[] esvaziar() {
        long[] saida = new long[tamanho()];

        for (int i = 0; i < saida.length; i++) saida[i] = tirarMenor();

        return saida;
    }
}

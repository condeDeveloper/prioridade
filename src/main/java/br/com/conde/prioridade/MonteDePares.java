package br.com.conde.prioridade;

import java.util.ArrayList;
import java.util.List;

/**
 * O MONTE PAREADO, de Fredman e companhia, 1986.
 *
 * <p>E uma arvore de grau qualquer, e a regra e uma so: a raiz e o menor. Juntar dois
 * montes e comparar as duas raizes e pendurar a maior como filha da menor. Uma
 * comparacao, e acabou.
 *
 * <p>Inserir e juntar com um monte de um elemento so: UMA comparacao, sempre. E por
 * isso que ele e tao bom em carga que insere muito e tira pouco, e e exatamente esse o
 * caso de Dijkstra e de A estrela.
 *
 * <p>Toda a conta esta na RETIRADA. Tirar a raiz deixa uma lista de filhos soltos, e
 * junta-los de volta e a unica decisao do algoritmo.
 *
 * <p>Junta-los da esquerda para a direita, um a um, pendura todos eles no mesmo no: a
 * arvore fica RASA e de GRAU altissimo, e a proxima retirada tem de emparelhar essa
 * multidao de novo. Junta-los DOIS A DOIS e depois da direita para a esquerda
 * distribui os filhos em varios niveis e mantem o grau baixo.
 *
 * <p>Eu tinha escrito aqui que a versao errada deixa uma LISTA ENCADEADA, e a medida
 * mostrou o contrario: ela fica mais RASA que a certa. O que explode nao e a
 * profundidade, e o grau da raiz.
 *
 * <p>Esse emparelhamento e o nome do monte, e e a unica coisa que ele faz de
 * diferente. A analise exata do custo dele e um problema aberto desde 1986: sabe-se
 * que esta entre log n e log n vezes alguma coisa pequena, e nao se sabe exatamente
 * onde.
 */
public final class MonteDePares implements Fila {

    private No raiz;
    private int tamanho;
    private final Contador contador = new Contador();
    private final boolean juntaEmFila;

    private static final class No {

        final long valor;
        final List<No> filhos = new ArrayList<>(2);

        No(long valor) {
            this.valor = valor;
        }
    }

    public MonteDePares() {
        this(false);
    }

    /**
     * @param juntaEmFila a versao plausivel: junta os filhos da esquerda para a
     *                    direita, um a um, em vez de dois a dois
     */
    public MonteDePares(boolean juntaEmFila) {
        this.juntaEmFila = juntaEmFila;
    }

    public static MonteDePares plausivel() {
        return new MonteDePares(true);
    }

    @Override
    public String nome() {
        return juntaEmFila ? "pareado em fila" : "pareado";
    }

    @Override
    public int tamanho() {
        return tamanho;
    }

    @Override
    public Contador contador() {
        return contador;
    }

    @Override
    public void limpar() {
        raiz = null;
        tamanho = 0;
        contador.zerar();
    }

    @Override
    public void inserir(long valor) {
        raiz = juntar(raiz, new No(valor));
        tamanho++;
    }

    @Override
    public long menor() {
        if (raiz == null) throw new IllegalStateException("fila vazia");

        return raiz.valor;
    }

    @Override
    public long tirarMenor() {
        long valor = menor();

        raiz = emparelhar(raiz.filhos);
        tamanho--;

        return valor;
    }

    /** Juntar dois montes e UMA comparacao: a maior raiz vira filha da menor. */
    private No juntar(No a, No b) {
        if (a == null) return b;
        if (b == null) return a;

        contador.comparou();

        No menor = a.valor <= b.valor ? a : b;
        No maior = menor == a ? b : a;

        menor.filhos.add(maior);
        contador.visitou();

        return menor;
    }

    /**
     * EMPARELHAR os filhos soltos depois de tirar a raiz.
     *
     * <p>A versao certa faz duas passadas: junta dois a dois da esquerda para a
     * direita, e depois junta os resultados da direita para a esquerda. A primeira
     * passada e o que impede a arvore de virar uma lista.
     *
     * <p>A versao plausivel faz uma passada so, da esquerda para a direita. Ela da a
     * mesma resposta e pendura todos os filhos no mesmo no: a proxima retirada passa a
     * custar o tamanho da fila inteira.
     */
    private No emparelhar(List<No> filhos) {
        if (filhos.isEmpty()) return null;

        if (juntaEmFila) {
            No acumulado = filhos.get(0);

            for (int i = 1; i < filhos.size(); i++) {
                contador.visitou();
                acumulado = juntar(acumulado, filhos.get(i));
            }

            return acumulado;
        }

        List<No> pares = new ArrayList<>((filhos.size() + 1) / 2);

        for (int i = 0; i + 1 < filhos.size(); i += 2) {
            contador.visitou();
            pares.add(juntar(filhos.get(i), filhos.get(i + 1)));
        }

        if (filhos.size() % 2 == 1) pares.add(filhos.get(filhos.size() - 1));

        No acumulado = pares.get(pares.size() - 1);

        for (int i = pares.size() - 2; i >= 0; i--) {
            contador.visitou();
            acumulado = juntar(pares.get(i), acumulado);
        }

        return acumulado;
    }

    /**
     * O MAIOR GRAU da arvore: quantos filhos tem o no mais carregado.
     *
     * <p>E esta a medida que separa as duas versoes de emparelhar, e nao a
     * profundidade. A versao errada pendura todos os filhos soltos no mesmo no, e o
     * grau dele vira o tamanho da fila.
     */
    public int maiorGrau() {
        if (raiz == null) return 0;

        int maior = 0;
        List<No> pilha = new ArrayList<>();

        pilha.add(raiz);

        while (!pilha.isEmpty()) {
            No no = pilha.remove(pilha.size() - 1);

            maior = Math.max(maior, no.filhos.size());
            pilha.addAll(no.filhos);
        }

        return maior;
    }

    /**
     * A PROFUNDIDADE da arvore.
     *
     * <p>Sem recursao: numa arvore degenerada pela versao plausivel a profundidade e o
     * tamanho da fila, e a versao recursiva estouraria a pilha exatamente no caso que
     * esta medida existe para mostrar.
     */
    public int profundidade() {
        if (raiz == null) return 0;

        int maior = 0;
        List<No> pilha = new ArrayList<>();
        List<Integer> niveis = new ArrayList<>();

        pilha.add(raiz);
        niveis.add(1);

        while (!pilha.isEmpty()) {
            No no = pilha.remove(pilha.size() - 1);
            int nivel = niveis.remove(niveis.size() - 1);

            maior = Math.max(maior, nivel);

            for (No filho : no.filhos) {
                pilha.add(filho);
                niveis.add(nivel + 1);
            }
        }

        return maior;
    }
}

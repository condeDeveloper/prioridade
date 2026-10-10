package br.com.conde.prioridade;

import java.util.Arrays;

/**
 * O MONTE BINARIO: uma arvore binaria quase completa guardada num VETOR.
 *
 * <p>A arvore nao existe. Os filhos de i sao 2i mais um e 2i mais dois, e o pai de i
 * e i menos um dividido por dois. Nao ha ponteiro nenhum, e e por isso que ele e o
 * mais rapido na pratica apesar de nao ser o melhor em nenhuma operacao isolada.
 *
 * <p>A PROPRIEDADE DO MONTE e uma linha: todo pai e menor ou igual aos filhos. Ela
 * nao diz nada sobre irmaos, e e por isso que um monte nao e uma lista ordenada e
 * custa muito menos para manter.
 *
 * <p>Duas operacoes sustentam tudo:
 *
 * <ul>
 *   <li>SUBIR: o elemento entra no fim e troca com o pai enquanto for menor. E o que
 *       a insercao faz.
 *   <li>DESCER: o elemento vai para a raiz e troca com o MENOR dos filhos enquanto
 *       for maior. E o que a retirada faz.
 * </ul>
 *
 * <p>"O menor dos filhos" e a parte que a versao plausivel deste repositorio erra, e
 * ela erra de um jeito que so aparece quando o filho da direita e menor que o da
 * esquerda.
 */
public final class MonteBinario implements Fila {

    private long[] vetor;
    private int tamanho;
    private final Contador contador = new Contador();
    private final boolean soOlhaAEsquerda;

    public MonteBinario() {
        this(16, false);
    }

    /**
     * @param soOlhaAEsquerda a versao plausivel: ao descer, compara so com o filho da
     *                        esquerda em vez de escolher o menor dos dois
     */
    public MonteBinario(int capacidade, boolean soOlhaAEsquerda) {
        this.vetor = new long[Math.max(1, capacidade)];
        this.soOlhaAEsquerda = soOlhaAEsquerda;
    }

    public static MonteBinario plausivel() {
        return new MonteBinario(16, true);
    }

    @Override
    public String nome() {
        return soOlhaAEsquerda ? "binario so a esquerda" : "binario";
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
        tamanho = 0;
        contador.zerar();
    }

    @Override
    public void inserir(long valor) {
        if (tamanho == vetor.length) vetor = Arrays.copyOf(vetor, vetor.length * 2);

        vetor[tamanho] = valor;
        subir(tamanho++);
    }

    @Override
    public long menor() {
        if (tamanho == 0) throw new IllegalStateException("fila vazia");

        return vetor[0];
    }

    @Override
    public long tirarMenor() {
        long raiz = menor();

        vetor[0] = vetor[--tamanho];

        if (tamanho > 0) descer(0);

        return raiz;
    }

    /** Sobe o elemento enquanto ele for menor que o pai. */
    private void subir(int i) {
        while (i > 0) {
            int pai = (i - 1) / 2;

            contador.comparou();

            if (vetor[i] >= vetor[pai]) break;

            trocar(i, pai);
            i = pai;
        }
    }

    /**
     * Desce o elemento trocando com o MENOR dos filhos.
     *
     * <p>Escolher o menor dos dois e o que garante a propriedade do monte. Trocar com
     * o da esquerda sem olhar o da direita deixa um pai maior que um filho, e e
     * exatamente esse o erro da versao plausivel.
     */
    private void descer(int i) {
        while (true) {
            int esquerda = 2 * i + 1;
            int direita = esquerda + 1;

            if (esquerda >= tamanho) break;

            int menorFilho = esquerda;

            if (!soOlhaAEsquerda && direita < tamanho) {
                contador.comparou();

                if (vetor[direita] < vetor[esquerda]) menorFilho = direita;
            }

            contador.comparou();

            if (vetor[i] <= vetor[menorFilho]) break;

            trocar(i, menorFilho);
            i = menorFilho;
        }
    }

    private void trocar(int a, int b) {
        long guarda = vetor[a];

        vetor[a] = vetor[b];
        vetor[b] = guarda;
        contador.trocou();
    }

    // ───────────────────────── as duas montagens ─────────────────────────

    /**
     * MONTAR DE BAIXO, que e a montagem de Floyd, de 1964.
     *
     * <p>Poe os n valores no vetor de qualquer jeito e desce cada no, a partir do
     * ultimo que tem filho ate a raiz. Custa menos de 2n comparacoes, e isso nao e
     * obvio: a maioria dos nos esta perto do fundo e desce pouco, e os poucos que
     * descem muito sao poucos.
     */
    public static MonteBinario montarDeBaixo(long[] valores) {
        MonteBinario monte = new MonteBinario(Math.max(1, valores.length), false);

        monte.vetor = valores.clone();
        monte.tamanho = valores.length;

        for (int i = valores.length / 2 - 1; i >= 0; i--) monte.descer(i);

        return monte;
    }

    /**
     * MONTAR INSERINDO, um por um, que e o jeito obvio.
     *
     * <p>Custa n log n comparacoes no pior caso. E o mesmo monte no fim, e nao e o
     * mesmo vetor: as duas montagens chegam a arranjos diferentes, e os dois sao
     * montes validos.
     */
    public static MonteBinario montarInserindo(long[] valores) {
        MonteBinario monte = new MonteBinario(Math.max(1, valores.length), false);

        for (long v : valores) monte.inserir(v);

        return monte;
    }

    /**
     * MONTAR DE CIMA, que e a versao plausivel da montagem.
     *
     * <p>Desce cada no do primeiro ao ultimo, em vez de do ultimo ao primeiro. Soa
     * igual, e e a ordem que importa: descer um no exige que as subarvores dele ja
     * sejam montes, e indo de cima para baixo elas ainda nao sao.
     *
     * <p>Ela acerta em vetor que ja esta quase ordenado e erra no resto, e a tabela
     * mede em quantos casos.
     */
    public static MonteBinario montarDeCima(long[] valores) {
        MonteBinario monte = new MonteBinario(Math.max(1, valores.length), false);

        monte.vetor = valores.clone();
        monte.tamanho = valores.length;

        for (int i = 0; i < valores.length; i++) monte.descer(i);

        return monte;
    }

    /** Uma copia do vetor interno, para o juiz conferir a propriedade do monte. */
    public long[] comoVetor() {
        return Arrays.copyOf(vetor, tamanho);
    }
}

package br.com.conde.prioridade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * O JUIZ: a FILA POR LISTA ORDENADA, que e a definicao escrita como codigo.
 *
 * <p>Ela guarda os valores numa lista e mantem a lista ordenada por insercao. Tirar o
 * menor e tirar o primeiro. Nao ha propriedade de monte, nao ha vetor, nao ha
 * distancia ate o vazio: ha uma lista ordenada e a definicao de "o menor".
 *
 * <p>Ela custa n por insercao e por isso nao serve para nada a nao ser julgar. E e
 * justamente por ser burra que ela julga: se os tres montes estivessem errados do
 * mesmo jeito, ela nao estaria errada junto.
 *
 * <p>E ha uma segunda conferencia, que nao olha para fila nenhuma: a PROPRIEDADE DO
 * MONTE no vetor. Ela pega erros que a sequencia de saida pode esconder, porque uma
 * fila pode devolver a ordem certa por acidente num caso pequeno e estar com o vetor
 * torto.
 */
public final class Juiz implements Fila {

    private final List<Long> ordenada = new ArrayList<>();
    private final Contador contador = new Contador();

    @Override
    public String nome() {
        return "lista ordenada";
    }

    @Override
    public int tamanho() {
        return ordenada.size();
    }

    @Override
    public Contador contador() {
        return contador;
    }

    @Override
    public void limpar() {
        ordenada.clear();
        contador.zerar();
    }

    @Override
    public void inserir(long valor) {
        int onde = 0;

        while (onde < ordenada.size()) {
            contador.comparou();

            if (ordenada.get(onde) > valor) break;

            onde++;
        }

        ordenada.add(onde, valor);
        contador.trocou();
    }

    @Override
    public long menor() {
        if (ordenada.isEmpty()) throw new IllegalStateException("fila vazia");

        return ordenada.get(0);
    }

    @Override
    public long tirarMenor() {
        long valor = menor();

        ordenada.remove(0);

        return valor;
    }

    // ───────────────────── a propriedade do monte ─────────────────────

    /**
     * @param vale    a propriedade do monte vale em todo no
     * @param onde    o primeiro indice em que ela falha, ou menos um
     * @param motivo  o que deu errado
     */
    public record Veredicto(boolean vale, int onde, String motivo) {

        public static final Veredicto CERTO = new Veredicto(true, -1, "");
    }

    /**
     * A PROPRIEDADE DO MONTE num vetor: todo pai e menor ou igual aos filhos.
     *
     * <p>Confere os DOIS filhos, e nao so o da esquerda. Um juiz que conferisse so a
     * esquerda aprovaria exatamente a versao plausivel que o repositorio existe para
     * pegar, e isso ja aconteceu comigo na primeira versao deste arquivo.
     */
    public static Veredicto conferirMonte(long[] vetor) {
        for (int i = 0; i < vetor.length; i++) {
            int esquerda = 2 * i + 1;
            int direita = esquerda + 1;

            if (esquerda < vetor.length && vetor[i] > vetor[esquerda])
                return new Veredicto(false, i,
                        "o pai " + vetor[i] + " em " + i + " e maior que o filho da esquerda " + vetor[esquerda]);

            if (direita < vetor.length && vetor[i] > vetor[direita])
                return new Veredicto(false, i,
                        "o pai " + vetor[i] + " em " + i + " e maior que o filho da direita " + vetor[direita]);
        }

        return Veredicto.CERTO;
    }

    /**
     * A fila devolve EXATAMENTE os mesmos valores, em ordem crescente?
     *
     * <p>Os mesmos valores, e nao os mesmos valores distintos: uma fila que perde uma
     * repeticao passaria numa comparacao de conjuntos e nao passa nesta.
     */
    public static Veredicto conferirSaida(long[] entrada, long[] saida) {
        if (entrada.length != saida.length)
            return new Veredicto(false, -1, "saiu com " + saida.length + " e entrou com " + entrada.length);

        for (int i = 1; i < saida.length; i++)
            if (saida[i - 1] > saida[i])
                return new Veredicto(false, i, "saiu " + saida[i - 1] + " antes de " + saida[i]);

        long[] esperado = entrada.clone();

        Arrays.sort(esperado);

        for (int i = 0; i < esperado.length; i++)
            if (esperado[i] != saida[i])
                return new Veredicto(false, i, "na posicao " + i + " saiu " + saida[i] + " e devia sair " + esperado[i]);

        return Veredicto.CERTO;
    }

    /** Monta a fila com os valores, esvazia e confere a saida contra a entrada. */
    public static Veredicto conferirFila(Fila fila, long[] valores) {
        fila.limpar();

        for (long v : valores) fila.inserir(v);

        if (fila.tamanho() != valores.length)
            return new Veredicto(false, -1, "a fila ficou com " + fila.tamanho() + " de " + valores.length);

        return conferirSaida(valores, fila.esvaziar());
    }
}

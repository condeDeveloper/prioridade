package br.com.conde.prioridade;

/**
 * O CONTADOR de operacoes, que e a unica coisa que este repositorio mede.
 *
 * <p>Comparacoes e trocas, nunca segundos. Segundo nao e medida: muda com a maquina,
 * com o que mais esta rodando e com o humor do coletor de lixo. A integracao continua
 * roda em tres sistemas, e tres numeros de segundos nao se comparam. Comparacao e a
 * mesma conta em qualquer lugar, e e o que o algoritmo realmente faz.
 *
 * <p>Comparacao e TROCA sao contadas separadas de proposito. Os tres montes deste
 * repositorio gastam quantidades muito diferentes de cada uma, e somar as duas num
 * numero so esconderia justamente o que os separa: o monte binario troca muito e
 * compara pouco, o pareado quase nao troca nada e compara em rajadas.
 */
public final class Contador {

    private long comparacoes;
    private long trocas;
    private long visitas;

    public void comparou() {
        comparacoes++;
    }

    public void comparou(long quantas) {
        comparacoes += quantas;
    }

    public void trocou() {
        trocas++;
    }

    public void visitou() {
        visitas++;
    }

    public long comparacoes() {
        return comparacoes;
    }

    public long trocas() {
        return trocas;
    }

    public long visitas() {
        return visitas;
    }

    public long total() {
        return comparacoes + trocas;
    }

    public void zerar() {
        comparacoes = 0;
        trocas = 0;
        visitas = 0;
    }

    @Override
    public String toString() {
        return comparacoes + " comparacoes, " + trocas + " trocas";
    }
}

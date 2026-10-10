package br.com.conde.prioridade;

/**
 * O MONTE ESQUERDISTA, de Crane, 1972.
 *
 * <p>Uma arvore binaria com a propriedade do monte e mais uma regra: a DISTANCIA ATE
 * O VAZIO mais proximo do filho da esquerda e sempre maior ou igual a do filho da
 * direita.
 *
 * <p>A consequencia e que o caminho mais a DIREITA e sempre curto: no maximo log de
 * n mais um. E toda a juncao de dois montes acontece ao longo desse caminho, o que
 * torna juntar dois montes uma operacao de log n em vez de n.
 *
 * <p>E essa a razao de ele existir. Juntar duas filas de prioridade num monte
 * binario custa reconstruir tudo; aqui custa log n. Inserir e tirar sao juncoes
 * disfarcadas, e por isso saem de graca.
 *
 * <p>Ele e deliberadamente desbalanceado. Uma arvore esquerdista pode ser uma lista
 * encadeada inteira pendurada a esquerda, e ela continua sendo um monte esquerdista
 * valido: a regra nao e sobre tamanho, e sobre a distancia ate o vazio.
 */
public final class MonteEsquerdista implements Fila {

    private No raiz;
    private int tamanho;
    private final Contador contador = new Contador();
    private final boolean semTrocarOsLados;

    private static final class No {

        final long valor;
        No esquerda;
        No direita;
        int distancia = 1;

        No(long valor) {
            this.valor = valor;
        }
    }

    public MonteEsquerdista() {
        this(false);
    }

    /**
     * @param semTrocarOsLados a versao plausivel: junta sem nunca trocar os filhos de
     *                         lado, e com isso perde a garantia do caminho curto
     */
    public MonteEsquerdista(boolean semTrocarOsLados) {
        this.semTrocarOsLados = semTrocarOsLados;
    }

    public static MonteEsquerdista plausivel() {
        return new MonteEsquerdista(true);
    }

    @Override
    public String nome() {
        return semTrocarOsLados ? "esquerdista sem trocar" : "esquerdista";
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

        raiz = juntar(raiz.esquerda, raiz.direita);
        tamanho--;

        return valor;
    }

    /**
     * JUNTAR dois montes esquerdistas, descendo pelo caminho da direita.
     *
     * <p>Sem recursao, com duas passadas: a primeira desce guardando o caminho, a
     * segunda volta consertando as distancias e trocando os lados onde preciso.
     *
     * <p>Sem recursao porque a versao plausivel deste mesmo arquivo produz arvores de
     * profundidade igual ao tamanho da fila, e a versao recursiva estouraria a pilha
     * exatamente no caso que a medida existe para mostrar. Ja tomei esse estouro no
     * macOS num repositorio anterior.
     */
    private No juntar(No a, No b) {
        if (a == null) return b;
        if (b == null) return a;

        // desce pelo caminho da direita guardando por onde passou
        java.util.List<No> caminho = new java.util.ArrayList<>();

        while (a != null && b != null) {
            contador.comparou();

            if (a.valor > b.valor) {
                No guarda = a;

                a = b;
                b = guarda;
            }

            caminho.add(a);
            contador.visitou();
            a = a.direita;
        }

        No juntado = a != null ? a : b;

        // volta consertando a distancia e trocando os lados
        for (int i = caminho.size() - 1; i >= 0; i--) {
            No no = caminho.get(i);

            no.direita = juntado;

            if (!semTrocarOsLados && distancia(no.esquerda) < distancia(no.direita)) {
                No guarda = no.esquerda;

                no.esquerda = no.direita;
                no.direita = guarda;
                contador.trocou();
            }

            no.distancia = distancia(no.direita) + 1;
            juntado = no;
        }

        return juntado;
    }

    private static int distancia(No no) {
        return no == null ? 0 : no.distancia;
    }

    /** O comprimento do caminho mais a DIREITA, que e o que a regra mantem curto. */
    public int caminhoDaDireita() {
        int quantos = 0;

        for (No no = raiz; no != null; no = no.direita) quantos++;

        return quantos;
    }

    /** A propriedade esquerdista vale em todo no? E o que o juiz confere. */
    public boolean ehEsquerdista() {
        if (semTrocarOsLados) return conferir(raiz);

        return conferir(raiz);
    }

    private static boolean conferir(No raiz) {
        java.util.List<No> pilha = new java.util.ArrayList<>();

        if (raiz != null) pilha.add(raiz);

        while (!pilha.isEmpty()) {
            No no = pilha.remove(pilha.size() - 1);

            if (distancia(no.esquerda) < distancia(no.direita)) return false;
            if (no.distancia != distancia(no.direita) + 1) return false;

            if (no.esquerda != null) pilha.add(no.esquerda);
            if (no.direita != null) pilha.add(no.direita);
        }

        return true;
    }
}

package br.com.conde.prioridade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * As MEDIDAS, em comparacoes e trocas, nunca em segundos.
 *
 * <p>Segundo nao e medida: muda com a maquina, com o que mais esta rodando e com o
 * humor do coletor de lixo. A integracao continua roda em tres sistemas, e tres
 * numeros de segundos nao se comparam.
 */
public final class Medidas {

    private Medidas() {
    }

    /**
     * O SORTEIO, escrito aqui em vez de vir da biblioteca.
     *
     * <p>"O monte pareado gastou 12.473 comparacoes" com vetores diferentes em cada
     * sistema nao e uma medida: sao tres medidas que ninguem pode comparar. O
     * deslocamento de 32 bits tira os bits de baixo, que num congruente linear
     * alternam par e impar a cada passo e fariam o vetor sair num ziguezague.
     */
    public static final class Sorteio {

        private long estado;

        public Sorteio(long semente) {
            this.estado = semente == 0 ? 1 : semente;
        }

        public int proximo() {
            estado = estado * 6364136223846793005L + 1442695040888963407L;
            return (int) (estado >>> 32);
        }

        public int ate(int limite) {
            return (int) (Integer.toUnsignedLong(proximo()) % limite);
        }
    }

    /** As filas certas, na ordem das tabelas. */
    public static List<Supplier<Fila>> certas() {
        return List.of(MonteBinario::new, MonteDePares::new, MonteEsquerdista::new);
    }

    /** As certas mais as plausiveis. */
    public static List<Supplier<Fila>> todas() {
        return List.of(MonteBinario::new, MonteBinario::plausivel,
                MonteDePares::new, MonteDePares::plausivel,
                MonteEsquerdista::new, MonteEsquerdista::plausivel);
    }

    // ───────────────────────── os vetores ─────────────────────────

    /**
     * Os VETORES que separam as filas, e o que cada um separa.
     *
     * <p>Vetor sorteado nao separa quase nada: toda fila se comporta parecido nele. O
     * que separa sao os vetores com estrutura, e o comentario de cada um diz qual.
     */
    public static Map<String, long[]> vetores() {
        Map<String, long[]> mapa = new LinkedHashMap<>();

        mapa.put("vazio", new long[0]);
        mapa.put("um so", new long[] {42});
        mapa.put("crescente", crescente(1_000));
        mapa.put("decrescente", decrescente(1_000));
        mapa.put("todos iguais", iguais(1_000));
        mapa.put("sorteado", sorteado(1_000, 1_000_000, 1));
        mapa.put("sorteado com repeticao", sorteado(1_000, 10, 2));
        mapa.put("serrote", serrote(1_000, 50));
        mapa.put("quase crescente", quaseCrescente(1_000, 20, 3));

        return mapa;
    }

    public static long[] crescente(int n) {
        long[] v = new long[n];

        for (int i = 0; i < n; i++) v[i] = i;

        return v;
    }

    public static long[] decrescente(int n) {
        long[] v = new long[n];

        for (int i = 0; i < n; i++) v[i] = n - i;

        return v;
    }

    public static long[] iguais(int n) {
        long[] v = new long[n];

        Arrays.fill(v, 7);

        return v;
    }

    public static long[] sorteado(int n, int maior, long semente) {
        Sorteio s = new Sorteio(semente);
        long[] v = new long[n];

        for (int i = 0; i < n; i++) v[i] = s.ate(maior);

        return v;
    }

    /** Sobe e desce em dentes: o caso em que a montagem de cima quase acerta. */
    public static long[] serrote(int n, int dente) {
        long[] v = new long[n];

        for (int i = 0; i < n; i++) v[i] = i % dente;

        return v;
    }

    /** Crescente com algumas trocas: o caso em que a versao errada quase sempre passa. */
    public static long[] quaseCrescente(int n, int trocas, long semente) {
        Sorteio s = new Sorteio(semente);
        long[] v = crescente(n);

        for (int i = 0; i < trocas; i++) {
            int a = s.ate(n);
            int b = s.ate(n);
            long guarda = v[a];

            v[a] = v[b];
            v[b] = guarda;
        }

        return v;
    }

    // ───────────────────── 1. contra a definicao ─────────────────────

    public record LinhaDefinicao(String vetor, int tamanho, Map<String, Boolean> passou,
                                 boolean todasAsCertasPassam, String primeiraFalha) {
    }

    /**
     * Cada fila contra o JUIZ: a saida tem de ser a entrada ordenada.
     *
     * <p>Os mesmos valores, na mesma quantidade, em ordem crescente. Qualquer erro na
     * propriedade do monte aparece aqui como um numero fora de lugar.
     */
    public static List<LinhaDefinicao> contraADefinicao() {
        List<LinhaDefinicao> linhas = new ArrayList<>();

        for (Map.Entry<String, long[]> e : vetores().entrySet()) {
            Map<String, Boolean> passou = new LinkedHashMap<>();
            boolean certasOk = true;
            String primeira = "";

            for (Supplier<Fila> fabrica : todas()) {
                Fila fila = fabrica.get();
                Juiz.Veredicto v = Juiz.conferirFila(fila, e.getValue());

                passou.put(fila.nome(), v.vale());

                if (!v.vale() && primeira.isEmpty()) primeira = fila.nome() + ": " + v.motivo();

                if (!v.vale() && !fila.nome().contains(" ")) certasOk = false;
            }

            linhas.add(new LinhaDefinicao(e.getKey(), e.getValue().length, passou, certasOk, primeira));
        }

        return linhas;
    }

    public record LinhaCaca(String familia, int vetores, Map<String, Integer> erros) {
    }

    /** As seis filas em familias de vetores sorteados. */
    public static List<LinhaCaca> cacarErros() {
        List<LinhaCaca> linhas = new ArrayList<>();

        for (int[] caso : new int[][] {{30, 1_000_000}, {30, 4}, {200, 1_000_000}, {200, 10}}) {
            Map<String, Integer> erros = new LinkedHashMap<>();

            for (Supplier<Fila> fabrica : todas()) erros.put(fabrica.get().nome(), 0);

            for (long semente = 1; semente <= 200; semente++) {
                long[] v = sorteado(caso[0], caso[1], semente);

                for (Supplier<Fila> fabrica : todas()) {
                    Fila fila = fabrica.get();

                    if (!Juiz.conferirFila(fila, v).vale()) erros.merge(fila.nome(), 1, Integer::sum);
                }
            }

            linhas.add(new LinhaCaca("sorteados de " + caso[0] + " ate " + caso[1], 200, erros));
        }

        return linhas;
    }

    // ───────────────────── 2. a montagem ao contrario ─────────────────────

    public record LinhaMontagem(String vetor, int tamanho,
                                boolean deBaixoVale, boolean inserindoVale, boolean deCimaVale,
                                long comparacoesDeBaixo, long comparacoesInserindo,
                                double comparacoesPorElemento) {
    }

    /**
     * As TRES montagens: de baixo, inserindo um a um, e de cima.
     *
     * <p>As duas primeiras estao certas e custam coisas bem diferentes. A terceira e a
     * versao plausivel: descer cada no do primeiro ao ultimo em vez de do ultimo ao
     * primeiro. A ordem e que importa, porque descer um no exige que as subarvores
     * dele ja sejam montes.
     */
    public static List<LinhaMontagem> asTresMontagens() {
        List<LinhaMontagem> linhas = new ArrayList<>();

        for (Map.Entry<String, long[]> e : vetores().entrySet()) {
            long[] v = e.getValue();

            MonteBinario deBaixo = MonteBinario.montarDeBaixo(v);
            MonteBinario inserindo = MonteBinario.montarInserindo(v);
            MonteBinario deCima = MonteBinario.montarDeCima(v);

            linhas.add(new LinhaMontagem(e.getKey(), v.length,
                    Juiz.conferirMonte(deBaixo.comoVetor()).vale(),
                    Juiz.conferirMonte(inserindo.comoVetor()).vale(),
                    Juiz.conferirMonte(deCima.comoVetor()).vale(),
                    deBaixo.contador().comparacoes(),
                    inserindo.contador().comparacoes(),
                    v.length == 0 ? 0 : (double) deBaixo.contador().comparacoes() / v.length));
        }

        return linhas;
    }

    public record LinhaCrescimentoDaMontagem(int tamanho, long deBaixo, long inserindo,
                                             double deBaixoPorElemento, double razao) {
    }

    /**
     * Como as duas montagens certas crescem com o tamanho.
     *
     * <p>Montar de baixo custa MENOS DE DUAS comparacoes por elemento, e isso vale
     * para qualquer tamanho. Montar inserindo custa log n por elemento.
     */
    public static List<LinhaCrescimentoDaMontagem> oCrescimentoDaMontagem() {
        List<LinhaCrescimentoDaMontagem> linhas = new ArrayList<>();

        for (int n : new int[] {100, 1_000, 10_000, 100_000, 1_000_000}) {
            long[] v = sorteado(n, 1_000_000_000, 7);

            long deBaixo = MonteBinario.montarDeBaixo(v).contador().comparacoes();
            long inserindo = MonteBinario.montarInserindo(v).contador().comparacoes();

            linhas.add(new LinhaCrescimentoDaMontagem(n, deBaixo, inserindo,
                    (double) deBaixo / n, (double) inserindo / deBaixo));
        }

        return linhas;
    }

    // ───────────────────── 3. o custo ─────────────────────

    public record LinhaCusto(String vetor, int tamanho, Map<String, Long> comparacoes,
                             Map<String, Long> trocas) {
    }

    /** Quantas comparacoes e trocas cada fila gasta para inserir tudo e esvaziar. */
    public static List<LinhaCusto> oCusto() {
        List<LinhaCusto> linhas = new ArrayList<>();

        for (Map.Entry<String, long[]> e : vetores().entrySet()) {
            Map<String, Long> comparacoes = new LinkedHashMap<>();
            Map<String, Long> trocas = new LinkedHashMap<>();

            for (Supplier<Fila> fabrica : certas()) {
                Fila fila = fabrica.get();

                for (long v : e.getValue()) fila.inserir(v);

                fila.esvaziar();

                comparacoes.put(fila.nome(), fila.contador().comparacoes());
                trocas.put(fila.nome(), fila.contador().trocas());
            }

            linhas.add(new LinhaCusto(e.getKey(), e.getValue().length, comparacoes, trocas));
        }

        return linhas;
    }

    public record LinhaInsercao(int tamanho, Map<String, Long> soInserindo, Map<String, Long> inserindoETirando) {
    }

    /**
     * A carga que SO INSERE contra a carga que insere e tira.
     *
     * <p>E a medida que separa o monte pareado dos outros: inserir nele e UMA
     * comparacao, sempre. Toda a conta dele esta na retirada.
     */
    public static List<LinhaInsercao> inserirContraTirar() {
        List<LinhaInsercao> linhas = new ArrayList<>();

        for (int n : new int[] {1_000, 10_000, 100_000}) {
            long[] v = sorteado(n, 1_000_000_000, 11);

            Map<String, Long> so = new LinkedHashMap<>();
            Map<String, Long> ambos = new LinkedHashMap<>();

            for (Supplier<Fila> fabrica : certas()) {
                Fila fila = fabrica.get();

                for (long x : v) fila.inserir(x);

                so.put(fila.nome(), fila.contador().comparacoes());

                fila.esvaziar();

                ambos.put(fila.nome(), fila.contador().comparacoes());
            }

            linhas.add(new LinhaInsercao(n, so, ambos));
        }

        return linhas;
    }

    // ───────────────────── 4. o emparelhamento ─────────────────────

    public record LinhaEmparelhamento(int tamanho, int profundidadeCerta, int profundidadePlausivel,
                                      int grauCerta, int grauPlausivel,
                                      long comparacoesCerta, long comparacoesPlausivel, double razao) {
    }

    /**
     * O que o EMPARELHAMENTO faz no monte pareado.
     *
     * <p>Junta-los dois a dois distribui os filhos em varios niveis e mantem o GRAU
     * baixo. Junta-los um a um da esquerda para a direita pendura todos eles no mesmo
     * no, e a proxima retirada tem de emparelhar essa multidao de novo.
     *
     * <p>A coluna da profundidade esta aqui porque eu esperava que ela explicasse a
     * diferenca, e ela nao explica: a versao errada fica MAIS RASA que a certa. Quem
     * explica e a coluna do grau.
     *
     * <p>As duas versoes dao a resposta certa. O que muda e o preco, e ele muda de
     * polinomial para quadratico.
     */
    public static List<LinhaEmparelhamento> oEmparelhamento() {
        List<LinhaEmparelhamento> linhas = new ArrayList<>();

        for (int n : new int[] {100, 500, 1_000, 2_000, 4_000}) {
            long[] v = sorteado(n, 1_000_000_000, 13);

            MonteDePares certa = new MonteDePares();
            MonteDePares plausivel = MonteDePares.plausivel();

            for (long x : v) {
                certa.inserir(x);
                plausivel.inserir(x);
            }

            // tira metade para o emparelhamento acontecer de verdade
            for (int i = 0; i < n / 2; i++) {
                certa.tirarMenor();
                plausivel.tirarMenor();
            }

            linhas.add(new LinhaEmparelhamento(n, certa.profundidade(), plausivel.profundidade(),
                    certa.maiorGrau(), plausivel.maiorGrau(),
                    certa.contador().comparacoes(), plausivel.contador().comparacoes(),
                    (double) plausivel.contador().comparacoes() / certa.contador().comparacoes()));
        }

        return linhas;
    }

    // ───────────────────── 5. o caminho da direita ─────────────────────

    public record LinhaEsquerdista(int tamanho, int caminhoCerto, int caminhoPlausivel,
                                   int tetoDoTeorema, boolean propriedadeVale,
                                   long comparacoesCerta, long comparacoesPlausivel) {
    }

    /**
     * O CAMINHO DA DIREITA do monte esquerdista contra o teto do teorema.
     *
     * <p>A propriedade esquerdista garante que esse caminho tem no maximo log de n
     * mais um. A versao que nao troca os lados perde a garantia, e o caminho cresce.
     */
    public static List<LinhaEsquerdista> oCaminhoDaDireita() {
        List<LinhaEsquerdista> linhas = new ArrayList<>();

        // o teto e dez mil porque a versao que nao troca os lados e QUADRATICA: com
        // cem mil ela gasta cinco bilhoes de comparacoes e a medida deixa de caber
        // num passo de integracao continua
        for (int n : new int[] {100, 500, 2_000, 10_000}) {
            long[] v = crescente(n);

            MonteEsquerdista certa = new MonteEsquerdista();
            MonteEsquerdista plausivel = MonteEsquerdista.plausivel();

            for (long x : v) {
                certa.inserir(x);
                plausivel.inserir(x);
            }

            int teto = (int) (Math.log(n + 1) / Math.log(2)) + 1;

            linhas.add(new LinhaEsquerdista(n, certa.caminhoDaDireita(), plausivel.caminhoDaDireita(),
                    teto, certa.ehEsquerdista(),
                    certa.contador().comparacoes(), plausivel.contador().comparacoes()));
        }

        return linhas;
    }
}

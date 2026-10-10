package br.com.conde.prioridade;

import java.util.Locale;

/**
 * O MEDIDOR: imprime as tabelas que o README mostra e que os testes conferem.
 *
 * <p>Toda tabela daqui tem um teste do lado que afirma os mesmos numeros. Texto nao
 * quebra quando deixa de ser verdade; teste quebra.
 */
public final class Medidor {

    private Medidor() {
    }

    public static void main(String[] args) {
        String comando = args.length > 0 ? args[0] : "tudo";

        if (comando.equals("definicao") || comando.equals("tudo")) definicao();
        if (comando.equals("erro") || comando.equals("tudo")) erro();
        if (comando.equals("montagem") || comando.equals("tudo")) montagem();
        if (comando.equals("custo") || comando.equals("tudo")) custo();
        if (comando.equals("chave") || comando.equals("tudo")) chave();
    }

    private static void definicao() {
        System.out.println();
        System.out.println("CADA FILA CONTRA O JUIZ: a saida tem de ser a entrada ordenada.");
        System.out.println();

        var linhas = Medidas.contraADefinicao();
        String[] nomes = linhas.get(0).passou().keySet().toArray(new String[0]);

        StringBuilder cabecalho = new StringBuilder(String.format("  %-24s %10s", "vetor", "tamanho"));

        for (String n : nomes) cabecalho.append(String.format(" %24s", n));

        System.out.println(cabecalho);

        for (var l : linhas) {
            StringBuilder linha = new StringBuilder(String.format("  %-24s %10d", l.vetor(), l.tamanho()));

            for (String n : nomes) linha.append(String.format(" %24s", l.passou().get(n) ? "passa" : "NAO"));

            System.out.println(linha);
        }

        System.out.println();
    }

    private static void erro() {
        System.out.println();
        System.out.println("A CACA, em 200 vetores sorteados de cada feitio.");
        System.out.println();

        var linhas = Medidas.cacarErros();
        String[] nomes = linhas.get(0).erros().keySet().toArray(new String[0]);

        StringBuilder cabecalho = new StringBuilder(String.format("  %-30s %9s", "familia", "vetores"));

        for (String n : nomes) cabecalho.append(String.format(" %24s", n));

        System.out.println(cabecalho);

        for (var l : linhas) {
            StringBuilder linha = new StringBuilder(String.format("  %-30s %9d", l.familia(), l.vetores()));

            for (String n : nomes) linha.append(String.format(" %24d", l.erros().get(n)));

            System.out.println(linha);
        }

        System.out.println();
    }

    private static void montagem() {
        System.out.println();
        System.out.println("AS TRES MONTAGENS: de baixo, inserindo um a um, e de cima.");
        System.out.println();
        System.out.printf("  %-24s %10s %12s %12s %10s %20s %22s %22s%n",
                "vetor", "tamanho", "de baixo", "inserindo", "de cima",
                "comp. de baixo", "comp. inserindo", "comp. por elemento");

        for (var l : Medidas.asTresMontagens())
            System.out.printf(Locale.ROOT, "  %-24s %10d %12s %12s %10s %20d %22d %22.2f%n",
                    l.vetor(), l.tamanho(),
                    l.deBaixoVale() ? "vale" : "NAO", l.inserindoVale() ? "vale" : "NAO",
                    l.deCimaVale() ? "vale" : "NAO",
                    l.comparacoesDeBaixo(), l.comparacoesInserindo(), l.comparacoesPorElemento());

        System.out.println();
        System.out.println("COMO AS DUAS MONTAGENS CERTAS CRESCEM.");
        System.out.println();
        System.out.printf("  %12s %16s %18s %24s %14s%n",
                "tamanho", "de baixo", "inserindo", "de baixo por elemento", "razao");

        for (var l : Medidas.oCrescimentoDaMontagem())
            System.out.printf(Locale.ROOT, "  %12d %16d %18d %24.2f %14.2f%n",
                    l.tamanho(), l.deBaixo(), l.inserindo(), l.deBaixoPorElemento(), l.razao());

        System.out.println();
    }

    private static void custo() {
        System.out.println();
        System.out.println("O CUSTO de inserir tudo e esvaziar, em comparacoes.");
        System.out.println();

        var linhas = Medidas.oCusto();
        String[] nomes = linhas.get(0).comparacoes().keySet().toArray(new String[0]);

        StringBuilder cabecalho = new StringBuilder(String.format("  %-24s %10s", "vetor", "tamanho"));

        for (String n : nomes) cabecalho.append(String.format(" %16s", n));

        for (String n : nomes) cabecalho.append(String.format(" %16s", "trocas " + n));

        System.out.println(cabecalho);

        for (var l : linhas) {
            StringBuilder linha = new StringBuilder(String.format("  %-24s %10d", l.vetor(), l.tamanho()));

            for (String n : nomes) linha.append(String.format(" %16d", l.comparacoes().get(n)));

            for (String n : nomes) linha.append(String.format(" %16d", l.trocas().get(n)));

            System.out.println(linha);
        }

        System.out.println();
        System.out.println("SO INSERINDO contra INSERINDO E TIRANDO, em comparacoes.");
        System.out.println();

        var insercoes = Medidas.inserirContraTirar();
        String[] filas = insercoes.get(0).soInserindo().keySet().toArray(new String[0]);

        StringBuilder cab = new StringBuilder(String.format("  %12s", "tamanho"));

        for (String n : filas) cab.append(String.format(" %18s", "so inserindo " + n.charAt(0)));

        for (String n : filas) cab.append(String.format(" %18s", "com tirar " + n.charAt(0)));

        System.out.println(cab);

        for (var l : insercoes) {
            StringBuilder linha = new StringBuilder(String.format("  %12d", l.tamanho()));

            for (String n : filas) linha.append(String.format(" %18d", l.soInserindo().get(n)));

            for (String n : filas) linha.append(String.format(" %18d", l.inserindoETirando().get(n)));

            System.out.println(linha);
        }

        System.out.println();
    }

    private static void chave() {
        System.out.println();
        System.out.println("O EMPARELHAMENTO do monte pareado: dois a dois contra um a um.");
        System.out.println();
        System.out.printf("  %10s %20s %24s %14s %18s %20s %24s %10s%n",
                "tamanho", "profundidade certa", "profundidade plausivel",
                "grau certa", "grau plausivel",
                "comparacoes certa", "comparacoes plausivel", "razao");

        for (var l : Medidas.oEmparelhamento())
            System.out.printf(Locale.ROOT, "  %10d %20d %24d %14d %18d %20d %24d %10.1f%n",
                    l.tamanho(), l.profundidadeCerta(), l.profundidadePlausivel(),
                    l.grauCerta(), l.grauPlausivel(),
                    l.comparacoesCerta(), l.comparacoesPlausivel(), l.razao());

        System.out.println();
        System.out.println("O CAMINHO DA DIREITA do monte esquerdista contra o teto do teorema.");
        System.out.println();
        System.out.printf("  %10s %18s %24s %18s %20s %22s %26s%n",
                "tamanho", "caminho certo", "caminho plausivel", "teto do teorema",
                "propriedade vale", "comparacoes certa", "comparacoes plausivel");

        for (var l : Medidas.oCaminhoDaDireita())
            System.out.printf("  %10d %18d %24d %18d %20s %22d %26d%n",
                    l.tamanho(), l.caminhoCerto(), l.caminhoPlausivel(), l.tetoDoTeorema(),
                    l.propriedadeVale() ? "sim" : "NAO",
                    l.comparacoesCerta(), l.comparacoesPlausivel());

        System.out.println();
    }
}

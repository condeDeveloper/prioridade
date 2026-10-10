package br.com.conde.prioridade;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Os testes das TABELAS que o medidor imprime.
 *
 * <p>Toda frase do README que e um numero esta aqui como afirmacao. Texto nao quebra
 * quando deixa de ser verdade; teste quebra.
 */
class MedidasTest {

    @Test
    @DisplayName("as tres filas certas passam em toda linha, e a do filho da esquerda nao")
    void todaLinhaPassa() {
        List<Medidas.LinhaDefinicao> linhas = Medidas.contraADefinicao();

        assertThat(linhas).hasSize(9);
        assertThat(linhas).allSatisfy(l -> assertThat(l.todasAsCertasPassam()).as(l.vetor()).isTrue());

        long erraram = linhas.stream().filter(l -> !l.passou().get("binario so a esquerda")).count();

        assertThat(erraram).isEqualTo(6);
    }

    /**
     * O ACHADO QUE ME CORRIGIU: das tres versoes plausiveis, so uma da resposta
     * errada.
     *
     * <p>As outras duas estao sempre certas e custam muito mais. E o tipo de defeito
     * que nenhum teste de corretude pega, porque nao ha nada de errado na resposta.
     */
    @Test
    @DisplayName("so a versao do filho da esquerda erra; as outras duas so custam mais")
    void soUmaDasTresErra() {
        List<Medidas.LinhaCaca> linhas = Medidas.cacarErros();

        assertThat(linhas).hasSize(4);
        assertThat(linhas).allSatisfy(l -> {
            assertThat(l.erros().get("binario")).as(l.familia()).isZero();
            assertThat(l.erros().get("pareado")).as(l.familia()).isZero();
            assertThat(l.erros().get("esquerdista")).as(l.familia()).isZero();

            assertThat(l.erros().get("binario so a esquerda")).as(l.familia()).isEqualTo(l.vetores());

            // as outras duas plausiveis nunca erram
            assertThat(l.erros().get("pareado em fila")).as(l.familia()).isZero();
            assertThat(l.erros().get("esquerdista sem trocar")).as(l.familia()).isZero();
        });
    }

    @Test
    @DisplayName("montar de baixo e inserindo valem sempre, e montar de cima nao")
    void asTresMontagens() {
        List<Medidas.LinhaMontagem> linhas = Medidas.asTresMontagens();

        assertThat(linhas).hasSize(9);
        assertThat(linhas).allSatisfy(l -> {
            assertThat(l.deBaixoVale()).as(l.vetor()).isTrue();
            assertThat(l.inserindoVale()).as(l.vetor()).isTrue();
        });

        long deCimaErrou = linhas.stream().filter(l -> !l.deCimaVale()).count();

        assertThat(deCimaErrou).isEqualTo(5);

        // e ela acerta no vetor crescente, que e onde ela nao tem o que descer
        assertThat(achar(linhas, "crescente").deCimaVale()).isTrue();
    }

    /**
     * MONTAR DE BAIXO custa menos de DUAS comparacoes por elemento, em qualquer
     * tamanho.
     *
     * <p>Isso nao e obvio: a maioria dos nos esta perto do fundo e desce pouco, e os
     * poucos que descem muito sao poucos. E o teorema de Floyd, de 1964.
     */
    @Test
    @DisplayName("montar de baixo fica abaixo de duas comparacoes por elemento em qualquer tamanho")
    void montarDeBaixoEhLinear() {
        List<Medidas.LinhaCrescimentoDaMontagem> linhas = Medidas.oCrescimentoDaMontagem();

        assertThat(linhas).hasSize(5);
        assertThat(linhas).allSatisfy(l ->
                assertThat(l.deBaixoPorElemento()).as("%d elementos", l.tamanho()).isLessThan(2.0));

        // e a razao entre as duas nao cresce: fica parada perto de um e dois decimos
        assertThat(linhas).allSatisfy(l ->
                assertThat(l.razao()).as("%d elementos", l.tamanho()).isBetween(1.0, 1.3));
    }

    /**
     * O OUTRO ACHADO QUE ME CORRIGIU: num vetor sorteado, montar inserindo custa pouco
     * mais que montar de baixo, e nao log n vezes mais.
     *
     * <p>A diferenca entre linear e n log n existe no PIOR CASO. Num vetor sorteado,
     * quase toda insercao para depois de uma ou duas comparacoes, porque o valor novo
     * quase nunca e menor que o caminho inteiro ate a raiz.
     */
    @Test
    @DisplayName("no vetor sorteado as duas montagens quase empatam, e no decrescente nao")
    void asDuasMontagensQuaseEmpatamNoSorteado() {
        List<Medidas.LinhaMontagem> linhas = Medidas.asTresMontagens();

        Medidas.LinhaMontagem sorteado = achar(linhas, "sorteado");
        Medidas.LinhaMontagem decrescente = achar(linhas, "decrescente");

        double razaoSorteado = (double) sorteado.comparacoesInserindo() / sorteado.comparacoesDeBaixo();
        double razaoDecrescente = (double) decrescente.comparacoesInserindo() / decrescente.comparacoesDeBaixo();

        assertThat(razaoSorteado).isLessThan(1.5);
        assertThat(razaoDecrescente).isGreaterThan(3.0);
    }

    /**
     * O EMPARELHAMENTO: a versao errada fica mais RASA e custa dezenas de vezes mais.
     *
     * <p>Eu esperava que ela ficasse mais funda, e a medida mostrou o contrario. O que
     * explode e o GRAU do no mais carregado, nao a profundidade.
     */
    @Test
    @DisplayName("a versao errada do emparelhamento fica mais rasa e com grau muito maior")
    void oEmparelhamentoExplodeNoGrau() {
        List<Medidas.LinhaEmparelhamento> linhas = Medidas.oEmparelhamento();

        assertThat(linhas).hasSize(5);

        Medidas.LinhaEmparelhamento maior = linhas.get(linhas.size() - 1);

        assertThat(maior.tamanho()).isEqualTo(4_000);
        assertThat(maior.profundidadePlausivel()).isLessThanOrEqualTo(maior.profundidadeCerta());
        assertThat(maior.grauPlausivel()).isGreaterThan(20 * maior.grauCerta());
        assertThat(maior.razao()).isGreaterThan(40.0);

        // e a razao so cresce com o tamanho
        for (int i = 1; i < linhas.size(); i++)
            assertThat(linhas.get(i).razao()).as("%d", linhas.get(i).tamanho())
                    .isGreaterThan(linhas.get(i - 1).razao());
    }

    /**
     * O CAMINHO DA DIREITA do monte esquerdista fica abaixo do teto do teorema, e a
     * versao que nao troca os lados vira uma lista do tamanho da fila.
     */
    @Test
    @DisplayName("o caminho da direita respeita o teto do teorema, e sem trocar os lados vira uma lista")
    void oCaminhoDaDireita() {
        List<Medidas.LinhaEsquerdista> linhas = Medidas.oCaminhoDaDireita();

        assertThat(linhas).hasSize(4);
        assertThat(linhas).allSatisfy(l -> {
            assertThat(l.caminhoCerto()).as("%d", l.tamanho()).isLessThanOrEqualTo(l.tetoDoTeorema());
            assertThat(l.propriedadeVale()).as("%d", l.tamanho()).isTrue();

            // sem trocar os lados o caminho e o tamanho inteiro da fila
            assertThat(l.caminhoPlausivel()).as("%d", l.tamanho()).isEqualTo(l.tamanho());
        });

        Medidas.LinhaEsquerdista maior = linhas.get(linhas.size() - 1);

        assertThat(maior.comparacoesPlausivel()).isGreaterThan(100 * maior.comparacoesCerta());
    }

    @Test
    @DisplayName("inserir no pareado e uma comparacao por elemento, sempre")
    void inserirNoPareadoEhUmaComparacao() {
        List<Medidas.LinhaInsercao> linhas = Medidas.inserirContraTirar();

        assertThat(linhas).hasSize(3);

        for (Medidas.LinhaInsercao l : linhas) {
            assertThat(l.soInserindo().get("pareado")).as("%d", l.tamanho()).isEqualTo(l.tamanho() - 1);

            // e o binario gasta mais que isso inserindo
            assertThat(l.soInserindo().get("binario")).as("%d", l.tamanho()).isGreaterThan(l.tamanho());

            // toda a conta do pareado esta na retirada
            assertThat(l.inserindoETirando().get("pareado")).as("%d", l.tamanho())
                    .isGreaterThan(5 * l.soInserindo().get("pareado"));
        }
    }

    @Test
    @DisplayName("o pareado nao troca nada, e o binario troca aos milhares")
    void oCustoNaTabela() {
        List<Medidas.LinhaCusto> linhas = Medidas.oCusto();

        assertThat(linhas).hasSize(9);

        Medidas.LinhaCusto sorteado = linhas.stream()
                .filter(l -> l.vetor().equals("sorteado"))
                .findFirst()
                .orElseThrow();

        // o pareado mexe em ponteiro, nao em vetor
        assertThat(sorteado.trocas().get("pareado")).isZero();
        assertThat(sorteado.trocas().get("binario")).isGreaterThan(1_000);
    }

    private static Medidas.LinhaMontagem achar(List<Medidas.LinhaMontagem> linhas, String vetor) {
        return linhas.stream().filter(l -> l.vetor().equals(vetor)).findFirst().orElseThrow();
    }
}

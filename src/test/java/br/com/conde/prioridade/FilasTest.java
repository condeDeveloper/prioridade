package br.com.conde.prioridade;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * As tres filas contra o JUIZ, que e a fila por lista ordenada.
 *
 * <p>Ela guarda os valores numa lista e mantem a lista ordenada. Nao ha propriedade
 * de monte, nao ha vetor, nao ha distancia ate o vazio: ha uma lista ordenada e a
 * definicao de "o menor". Se os tres montes estivessem errados do mesmo jeito, ela
 * nao estaria errada junto.
 */
class FilasTest {

    private static List<Supplier<Fila>> certas() {
        return List.of(MonteBinario::new, MonteDePares::new, MonteEsquerdista::new, Juiz::new);
    }

    @Test
    @DisplayName("as tres filas devolvem a entrada ordenada em todo vetor do repositorio")
    void asTresDevolvemOrdenado() {
        Medidas.vetores().forEach((nome, v) -> {
            for (Supplier<Fila> fabrica : certas()) {
                Fila fila = fabrica.get();
                Juiz.Veredicto veredicto = Juiz.conferirFila(fila, v);

                assertThat(veredicto.vale()).as("%s em %s: %s", fila.nome(), nome, veredicto.motivo()).isTrue();
            }
        });
    }

    @Test
    @DisplayName("e em oitocentos vetores sorteados de feitios bem diferentes")
    void asTresEmVetoresSorteados() {
        for (int[] caso : new int[][] {{30, 1_000_000}, {30, 4}, {200, 1_000_000}, {200, 10}})
            for (long semente = 1; semente <= 200; semente++) {
                long[] v = Medidas.sorteado(caso[0], caso[1], semente);

                for (Supplier<Fila> fabrica : certas()) {
                    Fila fila = fabrica.get();

                    assertThat(Juiz.conferirFila(fila, v).vale())
                            .as("%s, %d valores ate %d, semente %d", fila.nome(), caso[0], caso[1], semente)
                            .isTrue();
                }
            }
    }

    @Test
    @DisplayName("a fila vazia nao tem menor")
    void filaVazia() {
        for (Supplier<Fila> fabrica : certas()) {
            Fila fila = fabrica.get();

            assertThat(fila.vazia()).as(fila.nome()).isTrue();
            assertThatThrownBy(fila::menor).as(fila.nome()).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(fila::tirarMenor).as(fila.nome()).isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("valores repetidos saem todos, e na quantidade certa")
    void valoresRepetidos() {
        long[] v = {5, 5, 5, 1, 1, 9, 5, 1};

        for (Supplier<Fila> fabrica : certas()) {
            Fila fila = fabrica.get();

            for (long x : v) fila.inserir(x);

            assertThat(fila.esvaziar()).as(fila.nome()).containsExactly(1, 1, 1, 5, 5, 5, 5, 9);
        }
    }

    @Test
    @DisplayName("inserir e tirar intercalado mantem o menor certo o tempo todo")
    void intercalado() {
        Medidas.Sorteio sorteio = new Medidas.Sorteio(5);

        for (Supplier<Fila> fabrica : certas()) {
            Fila fila = fabrica.get();
            Juiz juiz = new Juiz();
            Medidas.Sorteio meu = new Medidas.Sorteio(5);

            for (int passo = 0; passo < 5_000; passo++)
                if (meu.ate(3) == 0 && !juiz.vazia()) {
                    assertThat(fila.tirarMenor()).as("%s no passo %d", fila.nome(), passo)
                            .isEqualTo(juiz.tirarMenor());
                } else {
                    long valor = meu.ate(1_000);

                    fila.inserir(valor);
                    juiz.inserir(valor);

                    assertThat(fila.menor()).as("%s no passo %d", fila.nome(), passo).isEqualTo(juiz.menor());
                }

            assertThat(fila.tamanho()).as(fila.nome()).isEqualTo(juiz.tamanho());
        }

        assertThat(sorteio.ate(2)).isBetween(0, 1);
    }

    /**
     * O ACHADO PRINCIPAL: descer comparando so com o filho da ESQUERDA quebra o monte.
     *
     * <p>A propriedade do monte exige que o pai seja menor que os DOIS filhos. Trocar
     * com o da esquerda sem olhar o da direita deixa um pai maior que um filho, e a
     * saida sai fora de ordem.
     */
    @Test
    @DisplayName("o monte que so olha o filho da esquerda erra em quase todo vetor")
    void soAEsquerdaErra() {
        int errou = 0;

        for (long semente = 1; semente <= 200; semente++) {
            long[] v = Medidas.sorteado(50, 1_000_000, semente);

            if (!Juiz.conferirFila(MonteBinario.plausivel(), v).vale()) errou++;
        }

        assertThat(errou).isEqualTo(200);

        // e no vetor de todos iguais ela acerta, porque nunca ha dois filhos diferentes
        assertThat(Juiz.conferirFila(MonteBinario.plausivel(), Medidas.iguais(500)).vale()).isTrue();
    }

    /**
     * O ACHADO QUE ME CORRIGIU: as outras duas versoes plausiveis NUNCA dao resposta
     * errada.
     *
     * <p>Eu montei tres versoes plausiveis esperando que as tres quebrassem. Uma
     * quebra; as outras duas estao sempre certas e so custam muito mais. E o tipo de
     * defeito que nenhum teste de corretude pega, porque nao ha nada de errado na
     * resposta.
     */
    @Test
    @DisplayName("as versoes plausiveis do pareado e do esquerdista nunca erram, so custam mais")
    void asOutrasDuasNuncaErram() {
        for (Supplier<Fila> fabrica : List.<Supplier<Fila>>of(MonteDePares::plausivel, MonteEsquerdista::plausivel)) {
            Medidas.vetores().forEach((nome, v) ->
                    assertThat(Juiz.conferirFila(fabrica.get(), v).vale())
                            .as("%s em %s", fabrica.get().nome(), nome)
                            .isTrue());

            for (long semente = 1; semente <= 100; semente++)
                assertThat(Juiz.conferirFila(fabrica.get(), Medidas.sorteado(100, 1_000, semente)).vale())
                        .as("%s na semente %d", fabrica.get().nome(), semente)
                        .isTrue();
        }
    }

    @Test
    @DisplayName("o juiz confere os dois filhos, e nao so o da esquerda")
    void oJuizConfereOsDoisFilhos() {
        // pai 5 com filho da esquerda 7 e da direita 3: so a direita quebra
        long[] torto = {5, 7, 3};

        Juiz.Veredicto v = Juiz.conferirMonte(torto);

        assertThat(v.vale()).isFalse();
        assertThat(v.motivo()).contains("filho da direita");

        assertThat(Juiz.conferirMonte(new long[] {1, 2, 3, 4, 5, 6, 7}).vale()).isTrue();
        assertThat(Juiz.conferirMonte(new long[] {}).vale()).isTrue();
    }

    @Test
    @DisplayName("o juiz pega saida que perde uma repeticao")
    void oJuizPegaRepeticaoPerdida() {
        assertThat(Juiz.conferirSaida(new long[] {1, 1, 2}, new long[] {1, 2}).vale()).isFalse();
        assertThat(Juiz.conferirSaida(new long[] {1, 1, 2}, new long[] {1, 2, 2}).vale()).isFalse();
        assertThat(Juiz.conferirSaida(new long[] {1, 1, 2}, new long[] {1, 1, 2}).vale()).isTrue();
    }
}

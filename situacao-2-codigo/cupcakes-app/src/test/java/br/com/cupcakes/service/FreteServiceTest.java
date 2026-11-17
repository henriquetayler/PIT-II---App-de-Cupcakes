package br.com.cupcakes.service;

import br.com.cupcakes.exception.NegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FreteServiceTest {

    private final FreteService service =
            new FreteService(List.of("387"), new BigDecimal("12.00"), new BigDecimal("150.00"), 2);

    @Test
    void cobraFreteFixoAbaixoDoMinimoParaFreteGratis() {
        FreteService.Frete frete = service.calcular("38700-000", new BigDecimal("100.00"));

        assertThat(frete.getValor()).isEqualByComparingTo("12.00");
        assertThat(frete.getPrazoDias()).isEqualTo(2);
    }

    @Test
    void pedidoExatamenteIgualA150AindaPagaFrete() {
        // Regra HU-08 RN#2: grátis apenas para pedidos ACIMA de R$ 150,00
        assertThat(service.calcular("38700000", new BigDecimal("150.00")).getValor())
                .isEqualByComparingTo("12.00");
    }

    @Test
    void pedidoAcimaDe150TemFreteGratis() {
        assertThat(service.calcular("38700-000", new BigDecimal("150.01")).getValor())
                .isEqualByComparingTo("0");
    }

    @Test
    void cepForaDaRegiaoAtendidaERejeitado() {
        assertThatThrownBy(() -> service.calcular("01001-000", new BigDecimal("50.00")))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Entrega não disponível para esta região");
    }

    @Test
    void cepMalFormadoERejeitado() {
        assertThatThrownBy(() -> service.calcular("123", new BigDecimal("50.00")))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Entrega não disponível para esta região");
    }
}

package br.com.cupcakes.service;

import br.com.cupcakes.exception.NegocioException;
import br.com.cupcakes.model.Cupom;
import br.com.cupcakes.repository.CupomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** HU-11: cupom de desconto. */
@ExtendWith(MockitoExtension.class)
class CupomServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 20);

    @Mock
    private CupomRepository cupomRepository;

    private CupomService service;

    @BeforeEach
    void setUp() {
        service = new CupomService(cupomRepository);
    }

    @Test
    void cupomValidoEhAceito() {
        Cupom c = new Cupom("BEMVINDO10", 10, HOJE.plusDays(1), 100);
        when(cupomRepository.findByCodigoIgnoreCase("bemvindo10")).thenReturn(Optional.of(c));

        assertThat(service.validar(" bemvindo10 ", HOJE)).isSameAs(c);
    }

    @Test
    void cupomValeAteOUltimoDiaDaValidade() {
        Cupom c = new Cupom("ULTIMODIA", 10, HOJE, 100);
        when(cupomRepository.findByCodigoIgnoreCase("ULTIMODIA")).thenReturn(Optional.of(c));

        assertThat(service.validar("ULTIMODIA", HOJE)).isSameAs(c);
    }

    @Test
    void cupomVencidoEhRecusado() {
        Cupom c = new Cupom("NATAL2025", 20, LocalDate.of(2025, 12, 31), 100);
        when(cupomRepository.findByCodigoIgnoreCase("NATAL2025")).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> service.validar("NATAL2025", HOJE))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Cupom inválido ou expirado");
    }

    @Test
    void cupomComLimiteDeUsoAtingidoEhRecusado() {
        Cupom c = new Cupom("ESGOTADO", 10, HOJE.plusDays(10), 2);
        c.setUsos(2);
        when(cupomRepository.findByCodigoIgnoreCase("ESGOTADO")).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> service.validar("ESGOTADO", HOJE))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Cupom inválido ou expirado");
    }

    @Test
    void cupomInexistenteEhRecusado() {
        when(cupomRepository.findByCodigoIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validar("NAOEXISTE", HOJE))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Cupom inválido ou expirado");
    }

    @Test
    void codigoVazioPedeOCodigo() {
        assertThatThrownBy(() -> service.validar("  ", HOJE))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Informe o código do cupom");
    }

    @Test
    void descontoEhPercentualDoSubtotalArredondado() {
        Cupom c = new Cupom("DOCE15", 15, HOJE, 10);
        // 15% de 33,10 = 4,965 -> 4,97
        assertThat(CupomService.calcularDesconto(c, new BigDecimal("33.10"))).isEqualByComparingTo("4.97");
    }

    @Test
    void registrarUsoSomaUm() {
        Cupom c = new Cupom("BEMVINDO10", 10, HOJE, 10);
        service.registrarUso(c);

        assertThat(c.getUsos()).isEqualTo(1);
        verify(cupomRepository).save(c);
    }
}

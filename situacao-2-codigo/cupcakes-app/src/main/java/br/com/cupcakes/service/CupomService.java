package br.com.cupcakes.service;

import br.com.cupcakes.exception.NegocioException;
import br.com.cupcakes.model.Cupom;
import br.com.cupcakes.repository.CupomRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** HU-11: validação e cálculo do cupom de desconto. */
@Service
public class CupomService {

    public static final String MENSAGEM_INVALIDO = "Cupom inválido ou expirado";   // HU-11 CA#2

    private final CupomRepository cupomRepository;

    public CupomService(CupomRepository cupomRepository) {
        this.cupomRepository = cupomRepository;
    }

    /** Devolve o cupom se ele existir, estiver ativo, dentro da validade e com usos disponíveis. */
    public Cupom validar(String codigo) {
        return validar(codigo, LocalDate.now());
    }

    Cupom validar(String codigo, LocalDate hoje) {
        if (codigo == null || codigo.isBlank()) {
            throw new NegocioException("Informe o código do cupom");
        }
        Cupom cupom = cupomRepository.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new NegocioException(MENSAGEM_INVALIDO));
        boolean vencido = cupom.getValidade().isBefore(hoje);
        boolean esgotado = cupom.getUsos() >= cupom.getLimiteUso();
        if (!cupom.isAtivo() || vencido || esgotado) {
            throw new NegocioException(MENSAGEM_INVALIDO);
        }
        return cupom;
    }

    /** Valor do desconto sobre o subtotal, arredondado para centavos. */
    public static BigDecimal calcularDesconto(Cupom cupom, BigDecimal subtotal) {
        if (cupom == null || subtotal == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        return subtotal.multiply(BigDecimal.valueOf(cupom.getPercentual()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /** Conta mais um uso (chamado ao finalizar o pedido). */
    public void registrarUso(Cupom cupom) {
        cupom.setUsos(cupom.getUsos() + 1);
        cupomRepository.save(cupom);
    }
}

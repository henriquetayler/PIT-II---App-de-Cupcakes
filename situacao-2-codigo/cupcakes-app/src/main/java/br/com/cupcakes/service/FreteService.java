package br.com.cupcakes.service;

import br.com.cupcakes.exception.NegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/** HU-08: cálculo de frete por CEP. */
@Service
public class FreteService {

    /** Resultado do cálculo. */
    public static class Frete {
        private final BigDecimal valor;
        private final int prazoDias;

        public Frete(BigDecimal valor, int prazoDias) {
            this.valor = valor;
            this.prazoDias = prazoDias;
        }

        public BigDecimal getValor() { return valor; }
        public int getPrazoDias() { return prazoDias; }
    }

    private final List<String> prefixosAtendidos;
    private final BigDecimal valorFixo;
    private final BigDecimal gratisAcima;
    private final int prazoDias;

    public FreteService(@Value("${loja.frete.prefixos}") List<String> prefixosAtendidos,
                        @Value("${loja.frete.valor}") BigDecimal valorFixo,
                        @Value("${loja.frete.gratis-acima}") BigDecimal gratisAcima,
                        @Value("${loja.frete.prazo-dias}") int prazoDias) {
        this.prefixosAtendidos = prefixosAtendidos;
        this.valorFixo = valorFixo;
        this.gratisAcima = gratisAcima;
        this.prazoDias = prazoDias;
    }

    public static String somenteDigitos(String cep) {
        return cep == null ? "" : cep.replaceAll("\\D", "");
    }

    public Frete calcular(String cep, BigDecimal subtotal) {
        String digitos = somenteDigitos(cep);
        boolean atendido = digitos.length() == 8
                && prefixosAtendidos.stream().anyMatch(p -> digitos.startsWith(p.trim()));
        if (!atendido) {
            throw new NegocioException("Entrega não disponível para esta região");
        }
        // HU-08 RN#2: frete grátis para pedidos ACIMA do valor configurado
        BigDecimal valor = subtotal.compareTo(gratisAcima) > 0 ? BigDecimal.ZERO.setScale(2) : valorFixo;
        return new Frete(valor, prazoDias);
    }
}

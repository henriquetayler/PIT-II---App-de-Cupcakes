package br.com.cupcakes.model;

/** HU-09 RN#1: formas de pagamento aceitas. */
public enum FormaPagamento {
    CARTAO_CREDITO("Cartão de crédito"),
    PIX("PIX"),
    BOLETO("Boleto");

    private final String rotulo;

    FormaPagamento(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}

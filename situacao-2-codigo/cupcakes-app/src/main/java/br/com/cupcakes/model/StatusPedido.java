package br.com.cupcakes.model;

/** HU-10 RN#1: status possíveis de um pedido. */
public enum StatusPedido {
    AGUARDANDO_PAGAMENTO("Aguardando pagamento"),
    EM_PREPARACAO("Em preparação"),
    SAIU_PARA_ENTREGA("Saiu para entrega"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    private final String rotulo;

    StatusPedido(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}

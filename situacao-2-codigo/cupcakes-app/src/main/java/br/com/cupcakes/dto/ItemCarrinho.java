package br.com.cupcakes.dto;

import br.com.cupcakes.model.Cupcake;

import java.math.BigDecimal;

/** Linha do carrinho: um cupcake e a quantidade escolhida. */
public class ItemCarrinho {

    private final Cupcake cupcake;
    private final int quantidade;

    public ItemCarrinho(Cupcake cupcake, int quantidade) {
        this.cupcake = cupcake;
        this.quantidade = quantidade;
    }

    public Cupcake getCupcake() { return cupcake; }
    public int getQuantidade() { return quantidade; }

    public BigDecimal getSubtotal() {
        return cupcake.getPreco().multiply(BigDecimal.valueOf(quantidade));
    }
}

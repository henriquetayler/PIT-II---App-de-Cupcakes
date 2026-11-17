package br.com.cupcakes.controller;

import br.com.cupcakes.service.Carrinho;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Disponibiliza a quantidade de itens do carrinho no cabeçalho de todas as páginas. */
@ControllerAdvice
public class GlobalModelAdvice {

    private final Carrinho carrinho;

    public GlobalModelAdvice(Carrinho carrinho) {
        this.carrinho = carrinho;
    }

    @ModelAttribute("qtdCarrinho")
    public int qtdCarrinho() {
        return carrinho.getTotalUnidades();
    }
}

package br.com.cupcakes.service;

import br.com.cupcakes.exception.NegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarrinhoTest {

    @Test
    void adicionarMesmoProdutoIncrementaQuantidade() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(1L, 1);
        carrinho.adicionar(1L, 1);

        assertThat(carrinho.quantidadeDe(1L)).isEqualTo(2);
        assertThat(carrinho.getTotalUnidades()).isEqualTo(2);
    }

    @Test
    void naoPermiteMaisDe50UnidadesPorItem() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(1L, 50);

        assertThatThrownBy(() -> carrinho.adicionar(1L, 1))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("50");
        assertThat(carrinho.quantidadeDe(1L)).isEqualTo(50);
    }

    @Test
    void quantidadeMinimaNaAlteracaoEUm() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(1L, 3);

        assertThatThrownBy(() -> carrinho.alterar(1L, 0))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("mínima");
        assertThat(carrinho.quantidadeDe(1L)).isEqualTo(3);
    }

    @Test
    void removerEsvaziaOCarrinho() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(1L, 2);
        carrinho.remover(1L);

        assertThat(carrinho.estaVazio()).isTrue();
    }
}

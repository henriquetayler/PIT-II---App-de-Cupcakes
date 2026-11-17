package br.com.cupcakes.service;

import br.com.cupcakes.exception.NegocioException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carrinho de compras guardado na sessão do usuário (HU-06 RN#1).
 * Guarda apenas id do cupcake -> quantidade.
 */
@Component
@SessionScope
public class Carrinho {

    public static final int QUANTIDADE_MAXIMA = 50;   // HU-06 RN#2
    public static final int QUANTIDADE_MINIMA = 1;    // HU-07 RN#1

    private final Map<Long, Integer> itens = new LinkedHashMap<>();

    /** HU-11 RN#1: só um cupom por pedido; aplicar outro substitui o anterior. */
    private String codigoCupom;

    public void adicionar(Long cupcakeId, int quantidade) {
        int nova = quantidadeDe(cupcakeId) + quantidade;
        validarLimites(nova);
        itens.put(cupcakeId, nova);
    }

    public void alterar(Long cupcakeId, int quantidade) {
        validarLimites(quantidade);
        if (itens.containsKey(cupcakeId)) {
            itens.put(cupcakeId, quantidade);
        }
    }

    public void remover(Long cupcakeId) {
        itens.remove(cupcakeId);
    }

    public void limpar() {
        itens.clear();
        codigoCupom = null;
    }

    public int quantidadeDe(Long cupcakeId) {
        return itens.getOrDefault(cupcakeId, 0);
    }

    public boolean estaVazio() {
        return itens.isEmpty();
    }

    public int getTotalUnidades() {
        return itens.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Cópia dos itens (id -> quantidade), na ordem em que foram adicionados. */
    public Map<Long, Integer> getItens() {
        return new LinkedHashMap<>(itens);
    }

    public String getCodigoCupom() {
        return codigoCupom;
    }

    public void setCodigoCupom(String codigoCupom) {
        this.codigoCupom = codigoCupom;
    }

    private void validarLimites(int quantidade) {
        if (quantidade < QUANTIDADE_MINIMA) {
            throw new NegocioException("A quantidade mínima é " + QUANTIDADE_MINIMA
                    + ". Para remover o item, use o ícone de exclusão.");
        }
        if (quantidade > QUANTIDADE_MAXIMA) {
            throw new NegocioException("A quantidade máxima por item é " + QUANTIDADE_MAXIMA + " unidades");
        }
    }
}

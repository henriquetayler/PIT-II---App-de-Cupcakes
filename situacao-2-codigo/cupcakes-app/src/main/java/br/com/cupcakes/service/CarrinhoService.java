package br.com.cupcakes.service;

import br.com.cupcakes.dto.ItemCarrinho;
import br.com.cupcakes.exception.NegocioException;
import br.com.cupcakes.model.Cupcake;
import br.com.cupcakes.model.Cupom;
import br.com.cupcakes.repository.CupcakeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** HU-06 (adicionar) e HU-07 (editar/remover). */
@Service
public class CarrinhoService {

    private final Carrinho carrinho;
    private final CupcakeRepository cupcakeRepository;
    private final CupomService cupomService;

    public CarrinhoService(Carrinho carrinho, CupcakeRepository cupcakeRepository, CupomService cupomService) {
        this.carrinho = carrinho;
        this.cupcakeRepository = cupcakeRepository;
        this.cupomService = cupomService;
    }

    public void adicionar(Long cupcakeId) {
        Cupcake cupcake = buscarDisponivel(cupcakeId);
        int nova = carrinho.quantidadeDe(cupcakeId) + 1;
        validarEstoque(cupcake, nova);
        carrinho.adicionar(cupcakeId, 1);
    }

    public void alterarQuantidade(Long cupcakeId, int quantidade) {
        Cupcake cupcake = buscarDisponivel(cupcakeId);
        validarEstoque(cupcake, quantidade);
        carrinho.alterar(cupcakeId, quantidade);
    }

    public void remover(Long cupcakeId) {
        carrinho.remover(cupcakeId);
    }

    /** Itens atuais; descarta automaticamente produtos que ficaram indisponíveis. */
    public List<ItemCarrinho> itens() {
        List<ItemCarrinho> resultado = new ArrayList<>();
        for (Map.Entry<Long, Integer> entrada : carrinho.getItens().entrySet()) {
            Cupcake cupcake = cupcakeRepository.findById(entrada.getKey()).orElse(null);
            if (cupcake == null || !cupcake.isAtivo()) {
                carrinho.remover(entrada.getKey());
            } else {
                resultado.add(new ItemCarrinho(cupcake, entrada.getValue()));
            }
        }
        return resultado;
    }

    public BigDecimal subtotal(List<ItemCarrinho> itens) {
        return itens.stream().map(ItemCarrinho::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void limpar() {
        carrinho.limpar();
    }

    // ---------- HU-11: cupom de desconto ----------

    public Cupom aplicarCupom(String codigo) {
        Cupom cupom = cupomService.validar(codigo);
        carrinho.setCodigoCupom(cupom.getCodigo());
        return cupom;
    }

    public void removerCupom() {
        carrinho.setCodigoCupom(null);
    }

    public String codigoCupom() {
        return carrinho.getCodigoCupom();
    }

    /** Cupom aplicado, revalidado agora (ele pode ter vencido ou esgotado desde que foi aplicado). */
    public Optional<Cupom> cupomAplicado() {
        String codigo = carrinho.getCodigoCupom();
        if (codigo == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(cupomService.validar(codigo));
        } catch (NegocioException e) {
            carrinho.setCodigoCupom(null);
            throw e;
        }
    }

    private Cupcake buscarDisponivel(Long id) {
        return cupcakeRepository.findById(id)
                .filter(Cupcake::isDisponivel)
                .orElseThrow(() -> new NegocioException("Produto indisponível"));
    }

    private void validarEstoque(Cupcake cupcake, int quantidade) {
        if (quantidade > cupcake.getEstoque()) {
            throw new NegocioException("Estoque insuficiente: restam apenas "
                    + cupcake.getEstoque() + " unidade(s) de " + cupcake.getNome());
        }
    }
}

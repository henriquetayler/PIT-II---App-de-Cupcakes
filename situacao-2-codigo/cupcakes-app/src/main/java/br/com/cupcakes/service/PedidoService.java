package br.com.cupcakes.service;

import br.com.cupcakes.dto.ItemCarrinho;
import br.com.cupcakes.exception.NegocioException;
import br.com.cupcakes.model.*;
import br.com.cupcakes.repository.CupcakeRepository;
import br.com.cupcakes.repository.PedidoRepository;
import br.com.cupcakes.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/** HU-09 (finalizar), HU-10 (acompanhar), HU-13 RN#1 (baixa de estoque) e HU-14 (gerenciar). */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CupcakeRepository cupcakeRepository;
    private final UsuarioRepository usuarioRepository;
    private final FreteService freteService;
    private final CupomService cupomService;

    public PedidoService(PedidoRepository pedidoRepository,
                         CupcakeRepository cupcakeRepository,
                         UsuarioRepository usuarioRepository,
                         FreteService freteService,
                         CupomService cupomService) {
        this.cupomService = cupomService;
        this.pedidoRepository = pedidoRepository;
        this.cupcakeRepository = cupcakeRepository;
        this.usuarioRepository = usuarioRepository;
        this.freteService = freteService;
    }

    /**
     * Registra o pedido e baixa o estoque. Se qualquer validação falhar, a transação
     * inteira é desfeita (o estoque não fica alterado pela metade).
     */
    @Transactional
    public Pedido finalizar(Long usuarioId, List<ItemCarrinho> itens, String cep,
                            String endereco, FormaPagamento forma) {
        return finalizar(usuarioId, itens, cep, endereco, forma, null);
    }

    /** Mesmo fluxo, com cupom de desconto opcional (HU-11). */
    @Transactional
    public Pedido finalizar(Long usuarioId, List<ItemCarrinho> itens, String cep,
                            String endereco, FormaPagamento forma, String codigoCupom) {
        if (itens == null || itens.isEmpty()) {
            throw new NegocioException("Seu carrinho está vazio");
        }
        if (endereco == null || endereco.isBlank()) {
            throw new NegocioException("Informe o endereço de entrega");
        }
        if (forma == null) {
            throw new NegocioException("Selecione a forma de pagamento");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado"));

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setFormaPagamento(forma);
        pedido.setEnderecoEntrega(endereco.trim());
        pedido.setCep(FreteService.somenteDigitos(cep));
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemCarrinho item : itens) {
            // Relê do banco para validar o estoque atual, não o da sessão
            Cupcake cupcake = cupcakeRepository.findById(item.getCupcake().getId())
                    .orElseThrow(() -> new NegocioException("Produto não encontrado"));
            if (!cupcake.isAtivo() || cupcake.getEstoque() < item.getQuantidade()) {
                throw new NegocioException("Estoque insuficiente para: " + cupcake.getNome());
            }
            cupcake.setEstoque(cupcake.getEstoque() - item.getQuantidade());   // HU-13 RN#1
            cupcakeRepository.save(cupcake);

            ItemPedido ip = new ItemPedido();
            ip.setCupcake(cupcake);
            ip.setQuantidade(item.getQuantidade());
            ip.setPrecoUnitario(cupcake.getPreco());
            pedido.adicionarItem(ip);
            subtotal = subtotal.add(ip.getSubtotal());
        }

        // HU-11: desconto sobre o subtotal; o frete é calculado sobre o valor já com desconto
        BigDecimal desconto = BigDecimal.ZERO.setScale(2);
        if (codigoCupom != null && !codigoCupom.isBlank()) {
            Cupom cupom = cupomService.validar(codigoCupom);
            desconto = CupomService.calcularDesconto(cupom, subtotal);
            cupomService.registrarUso(cupom);
            pedido.setCupom(cupom);
        }
        BigDecimal subtotalComDesconto = subtotal.subtract(desconto);

        FreteService.Frete frete = freteService.calcular(cep, subtotalComDesconto);
        pedido.setSubtotal(subtotal);
        pedido.setDesconto(desconto);
        pedido.setFrete(frete.getValor());
        pedido.setTotal(subtotalComDesconto.add(frete.getValor()));
        pedido.setPrazoEntregaDias(frete.getPrazoDias());
        if (forma == FormaPagamento.BOLETO) {
            pedido.setVencimentoBoleto(somarDiasUteis(LocalDate.now(), 2));   // HU-09 RN#2
        }
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarDoUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioIdOrderByDataHoraDesc(usuarioId);
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByDataHoraDesc();
    }

    /** HU-14: somente o administrador chama este método (rota /admin protegida). */
    @Transactional
    public Pedido atualizarStatus(Long pedidoId, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NegocioException("Pedido não encontrado"));
        if (pedido.getStatus() == StatusPedido.CANCELADO && novoStatus != StatusPedido.CANCELADO) {
            throw new NegocioException("Pedidos cancelados não podem ter o status revertido");   // HU-14 RN#2
        }
        if (novoStatus == StatusPedido.CANCELADO && pedido.getStatus() != StatusPedido.CANCELADO) {
            for (ItemPedido item : pedido.getItens()) {   // devolve o estoque
                Cupcake c = item.getCupcake();
                c.setEstoque(c.getEstoque() + item.getQuantidade());
                cupcakeRepository.save(c);
            }
        }
        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    static LocalDate somarDiasUteis(LocalDate inicio, int dias) {
        LocalDate data = inicio;
        int somados = 0;
        while (somados < dias) {
            data = data.plusDays(1);
            if (data.getDayOfWeek() != DayOfWeek.SATURDAY && data.getDayOfWeek() != DayOfWeek.SUNDAY) {
                somados++;
            }
        }
        return data;
    }
}

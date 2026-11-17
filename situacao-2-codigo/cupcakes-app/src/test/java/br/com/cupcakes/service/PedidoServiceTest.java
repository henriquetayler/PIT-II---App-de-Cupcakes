package br.com.cupcakes.service;

import br.com.cupcakes.dto.ItemCarrinho;
import br.com.cupcakes.exception.NegocioException;
import br.com.cupcakes.model.*;
import br.com.cupcakes.repository.CupcakeRepository;
import br.com.cupcakes.repository.CupomRepository;
import br.com.cupcakes.repository.PedidoRepository;
import br.com.cupcakes.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private CupcakeRepository cupcakeRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CupomRepository cupomRepository;

    private final FreteService freteService =
            new FreteService(List.of("387"), new BigDecimal("12.00"), new BigDecimal("150.00"), 2);

    private PedidoService service;

    @BeforeEach
    void setUp() {
        service = new PedidoService(pedidoRepository, cupcakeRepository, usuarioRepository, freteService,
                new CupomService(cupomRepository));
    }

    private Cupcake cupcake(long id, String nome, String preco, int estoque) {
        Cupcake c = new Cupcake();
        c.setId(id);
        c.setNome(nome);
        c.setDescricao("desc");
        c.setPreco(new BigDecimal(preco));
        c.setEstoque(estoque);
        return c;
    }

    private void prepararMocksDeFinalizacao(Cupcake c) {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(new Usuario()));
        when(cupcakeRepository.findById(c.getId())).thenReturn(Optional.of(c));
    }

    // ---------- HU-09 / HU-13 RN#1 ----------

    @Test
    void finalizarPedidoBaixaEstoqueECalculaTotais() {
        Cupcake c = cupcake(1, "Red Velvet", "11.00", 10);
        prepararMocksDeFinalizacao(c);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido p = service.finalizar(1L, List.of(new ItemCarrinho(c, 3)),
                "38700-000", "Rua A, 10", FormaPagamento.PIX);

        assertThat(c.getEstoque()).isEqualTo(7);
        assertThat(p.getSubtotal()).isEqualByComparingTo("33.00");
        assertThat(p.getFrete()).isEqualByComparingTo("12.00");
        assertThat(p.getTotal()).isEqualByComparingTo("45.00");
        assertThat(p.getStatus()).isEqualTo(StatusPedido.AGUARDANDO_PAGAMENTO);
        assertThat(p.getVencimentoBoleto()).isNull();
        assertThat(p.getItens()).hasSize(1);
    }

    @Test
    void pedidoComBoletoTemVencimentoEmDiaUtilFuturo() {
        Cupcake c = cupcake(1, "Red Velvet", "11.00", 10);
        prepararMocksDeFinalizacao(c);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido p = service.finalizar(1L, List.of(new ItemCarrinho(c, 1)),
                "38700-000", "Rua A, 10", FormaPagamento.BOLETO);

        assertThat(p.getVencimentoBoleto()).isAfter(LocalDate.now());
    }

    @Test
    void estoqueInsuficienteImpedeOPedido() {
        Cupcake c = cupcake(1, "Red Velvet", "11.00", 2);
        prepararMocksDeFinalizacao(c);

        assertThatThrownBy(() -> service.finalizar(1L, List.of(new ItemCarrinho(c, 3)),
                "38700-000", "Rua A, 10", FormaPagamento.PIX))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Estoque insuficiente para: Red Velvet");
    }

    @Test
    void cepForaDaRegiaoImpedeOPedido() {
        Cupcake c = cupcake(1, "Red Velvet", "11.00", 10);
        prepararMocksDeFinalizacao(c);

        assertThatThrownBy(() -> service.finalizar(1L, List.of(new ItemCarrinho(c, 1)),
                "01001-000", "Rua A, 10", FormaPagamento.PIX))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Entrega não disponível para esta região");
    }

    @Test
    void carrinhoVazioImpedeOPedido() {
        assertThatThrownBy(() -> service.finalizar(1L, List.of(), "38700-000", "Rua A", FormaPagamento.PIX))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Seu carrinho está vazio");
    }

    // ---------- HU-11 ----------

    @Test
    void pedidoComCupomAplicaDescontoEContaOUso() {
        Cupcake c = cupcake(1, "Red Velvet", "11.00", 10);
        prepararMocksDeFinalizacao(c);
        Cupom cupom = new Cupom("BEMVINDO10", 10, LocalDate.now().plusDays(30), 100);
        when(cupomRepository.findByCodigoIgnoreCase("BEMVINDO10")).thenReturn(Optional.of(cupom));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido p = service.finalizar(1L, List.of(new ItemCarrinho(c, 3)),
                "38700-000", "Rua A, 10", FormaPagamento.PIX, "BEMVINDO10");

        assertThat(p.getSubtotal()).isEqualByComparingTo("33.00");
        assertThat(p.getDesconto()).isEqualByComparingTo("3.30");
        assertThat(p.getFrete()).isEqualByComparingTo("12.00");
        assertThat(p.getTotal()).isEqualByComparingTo("41.70");
        assertThat(p.getCupom()).isSameAs(cupom);
        assertThat(cupom.getUsos()).isEqualTo(1);
    }

    @Test
    void pedidoSemCupomTemDescontoZero() {
        Cupcake c = cupcake(1, "Red Velvet", "11.00", 10);
        prepararMocksDeFinalizacao(c);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido p = service.finalizar(1L, List.of(new ItemCarrinho(c, 1)),
                "38700-000", "Rua A, 10", FormaPagamento.PIX);

        assertThat(p.getDesconto()).isEqualByComparingTo("0.00");
        assertThat(p.getCupom()).isNull();
    }

    // ---------- HU-14 ----------

    private Pedido pedidoCom(Cupcake c, int qtd, StatusPedido status) {
        Pedido p = new Pedido();
        p.setId(5L);
        p.setStatus(status);
        ItemPedido item = new ItemPedido();
        item.setCupcake(c);
        item.setQuantidade(qtd);
        item.setPrecoUnitario(c.getPreco());
        p.adicionarItem(item);
        return p;
    }

    @Test
    void cancelarPedidoDevolveOEstoque() {
        Cupcake c = cupcake(1, "Red Velvet", "11.00", 7);
        Pedido p = pedidoCom(c, 3, StatusPedido.EM_PREPARACAO);
        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(p));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        service.atualizarStatus(5L, StatusPedido.CANCELADO);

        assertThat(p.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(c.getEstoque()).isEqualTo(10);
    }

    @Test
    void pedidoCanceladoNaoPodeSerReaberto() {
        Cupcake c = cupcake(1, "Red Velvet", "11.00", 10);
        Pedido p = pedidoCom(c, 3, StatusPedido.CANCELADO);
        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.atualizarStatus(5L, StatusPedido.EM_PREPARACAO))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Pedidos cancelados não podem ter o status revertido");
    }

    @Test
    void somaDiasUteisPulaOFimDeSemana() {
        // 20/11/2026 é sexta-feira: +2 dias úteis = terça 24/11/2026
        assertThat(PedidoService.somarDiasUteis(LocalDate.of(2026, 11, 20), 2))
                .isEqualTo(LocalDate.of(2026, 11, 24));
    }
}

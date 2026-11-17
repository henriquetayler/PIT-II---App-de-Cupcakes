package br.com.cupcakes.controller;

import br.com.cupcakes.config.UsuarioLogado;
import br.com.cupcakes.dto.ItemCarrinho;
import br.com.cupcakes.exception.NegocioException;
import br.com.cupcakes.model.Cupom;
import br.com.cupcakes.model.FormaPagamento;
import br.com.cupcakes.service.CupomService;
import br.com.cupcakes.service.CarrinhoService;
import br.com.cupcakes.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** HU-09 (finalizar pedido) e HU-10 (acompanhar pedidos). Rotas protegidas por login. */
@Controller
public class PedidoController {

    private final CarrinhoService carrinhoService;
    private final PedidoService pedidoService;

    public PedidoController(CarrinhoService carrinhoService, PedidoService pedidoService) {
        this.carrinhoService = carrinhoService;
        this.pedidoService = pedidoService;
    }

    @GetMapping("/checkout")
    public String checkout(Model model, RedirectAttributes redirect) {
        List<ItemCarrinho> itens = carrinhoService.itens();
        if (itens.isEmpty()) {
            redirect.addFlashAttribute("erro", "Seu carrinho está vazio");
            return "redirect:/carrinho";
        }
        preencher(model, itens);
        return "checkout";
    }

    @PostMapping("/checkout")
    public String finalizar(@RequestParam String cep, @RequestParam String endereco,
                            @RequestParam(required = false) FormaPagamento formaPagamento,
                            HttpSession sessao, Model model, RedirectAttributes redirect) {
        UsuarioLogado usuario = (UsuarioLogado) sessao.getAttribute(UsuarioLogado.ATRIBUTO_SESSAO);
        List<ItemCarrinho> itens = carrinhoService.itens();
        try {
            pedidoService.finalizar(usuario.getId(), itens, cep, endereco, formaPagamento,
                    carrinhoService.codigoCupom());
            carrinhoService.limpar();
            redirect.addFlashAttribute("sucesso", "Pedido realizado com sucesso! Acompanhe o status abaixo.");
            return "redirect:/pedidos";
        } catch (NegocioException e) {
            preencher(model, itens);
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("cep", cep);
            model.addAttribute("endereco", endereco);
            model.addAttribute("formaSelecionada", formaPagamento);
            return "checkout";
        }
    }

    @GetMapping("/pedidos")
    public String meusPedidos(HttpSession sessao, Model model) {
        UsuarioLogado usuario = (UsuarioLogado) sessao.getAttribute(UsuarioLogado.ATRIBUTO_SESSAO);
        model.addAttribute("pedidos", pedidoService.listarDoUsuario(usuario.getId()));
        return "pedidos";
    }

    private void preencher(Model model, List<ItemCarrinho> itens) {
        BigDecimal subtotal = carrinhoService.subtotal(itens);
        BigDecimal desconto = BigDecimal.ZERO;
        try {
            Optional<Cupom> cupom = carrinhoService.cupomAplicado();
            if (cupom.isPresent()) {
                desconto = CupomService.calcularDesconto(cupom.get(), subtotal);
                model.addAttribute("cupom", cupom.get());
            }
        } catch (NegocioException e) {
            model.addAttribute("erro", e.getMessage() + ". O cupom foi retirado do carrinho.");
        }
        model.addAttribute("itens", itens);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("desconto", desconto);
        model.addAttribute("subtotalComDesconto", subtotal.subtract(desconto));
        model.addAttribute("formas", FormaPagamento.values());
    }
}

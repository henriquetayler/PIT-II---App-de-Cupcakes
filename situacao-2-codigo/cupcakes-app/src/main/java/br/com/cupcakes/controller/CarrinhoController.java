package br.com.cupcakes.controller;

import br.com.cupcakes.dto.ItemCarrinho;
import br.com.cupcakes.exception.NegocioException;
import br.com.cupcakes.model.Cupom;
import br.com.cupcakes.service.CarrinhoService;
import br.com.cupcakes.service.CupomService;
import br.com.cupcakes.service.FreteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** HU-06 (adicionar), HU-07 (editar/remover), HU-08 (calcular frete) e HU-11 (cupom). */
@Controller
public class CarrinhoController {

    private final CarrinhoService carrinhoService;
    private final FreteService freteService;

    public CarrinhoController(CarrinhoService carrinhoService, FreteService freteService) {
        this.carrinhoService = carrinhoService;
        this.freteService = freteService;
    }

    @GetMapping("/carrinho")
    public String ver(Model model) {
        preencher(model);
        return "carrinho";
    }

    @PostMapping("/carrinho/adicionar")
    public String adicionar(@RequestParam Long id, @RequestHeader(value = "Referer", required = false) String origem,
                            RedirectAttributes redirect) {
        try {
            carrinhoService.adicionar(id);
            redirect.addFlashAttribute("sucesso", "Item adicionado ao carrinho");
        } catch (NegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:" + (origem == null || origem.isBlank() ? "/" : origem);
    }

    @PostMapping("/carrinho/alterar")
    public String alterar(@RequestParam Long id, @RequestParam int quantidade, RedirectAttributes redirect) {
        try {
            carrinhoService.alterarQuantidade(id, quantidade);
        } catch (NegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/carrinho";
    }

    @PostMapping("/carrinho/remover")
    public String remover(@RequestParam Long id) {
        carrinhoService.remover(id);
        return "redirect:/carrinho";
    }

    @PostMapping("/carrinho/frete")
    public String frete(@RequestParam String cep, Model model) {
        BigDecimal base = preencher(model);
        model.addAttribute("cep", cep);
        try {
            FreteService.Frete frete = freteService.calcular(cep, base);
            model.addAttribute("frete", frete);
            model.addAttribute("totalComFrete", base.add(frete.getValor()));
        } catch (NegocioException e) {
            model.addAttribute("erro", e.getMessage());
        }
        return "carrinho";
    }

    // ---------- HU-11: cupom de desconto ----------

    @PostMapping("/carrinho/cupom")
    public String aplicarCupom(@RequestParam String codigo, RedirectAttributes redirect) {
        try {
            Cupom cupom = carrinhoService.aplicarCupom(codigo);
            redirect.addFlashAttribute("sucesso", "Cupom " + cupom.getCodigo() + " aplicado: "
                    + cupom.getPercentual() + "% de desconto");
        } catch (NegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/carrinho";
    }

    @PostMapping("/carrinho/cupom/remover")
    public String removerCupom(RedirectAttributes redirect) {
        carrinhoService.removerCupom();
        redirect.addFlashAttribute("sucesso", "Cupom removido");
        return "redirect:/carrinho";
    }

    /** Preenche itens, subtotal e desconto; devolve o valor sobre o qual o frete é calculado. */
    private BigDecimal preencher(Model model) {
        List<ItemCarrinho> itens = carrinhoService.itens();
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
        BigDecimal subtotalComDesconto = subtotal.subtract(desconto);
        model.addAttribute("itens", itens);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("desconto", desconto);
        model.addAttribute("subtotalComDesconto", subtotalComDesconto);
        return subtotalComDesconto;
    }
}

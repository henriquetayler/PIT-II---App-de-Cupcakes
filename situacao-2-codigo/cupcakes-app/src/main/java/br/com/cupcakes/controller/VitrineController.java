package br.com.cupcakes.controller;

import br.com.cupcakes.exception.NegocioException;
import br.com.cupcakes.service.CupcakeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HU-03 (vitrine), HU-04 (filtro por categoria) e HU-05 (detalhe do produto). */
@Controller
public class VitrineController {

    private final CupcakeService cupcakeService;

    public VitrineController(CupcakeService cupcakeService) {
        this.cupcakeService = cupcakeService;
    }

    @GetMapping("/")
    public String vitrine(@RequestParam(required = false) Long categoria, Model model) {
        model.addAttribute("cupcakes", cupcakeService.vitrine(categoria));
        model.addAttribute("categorias", cupcakeService.categorias());
        model.addAttribute("categoriaSelecionada", categoria);
        return "vitrine";
    }

    @GetMapping("/cupcakes/{id}")
    public String detalhe(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("cupcake", cupcakeService.buscarParaDetalhe(id));
            return "detalhe";
        } catch (NegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/";
        }
    }
}

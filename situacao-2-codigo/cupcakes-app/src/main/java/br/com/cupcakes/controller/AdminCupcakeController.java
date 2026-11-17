package br.com.cupcakes.controller;

import br.com.cupcakes.dto.CupcakeForm;
import br.com.cupcakes.exception.NegocioException;
import br.com.cupcakes.model.Cupcake;
import br.com.cupcakes.service.CupcakeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HU-12 (catálogo) e HU-13 (estoque). Acesso restrito ao administrador (AuthInterceptor). */
@Controller
public class AdminCupcakeController {

    private final CupcakeService cupcakeService;

    public AdminCupcakeController(CupcakeService cupcakeService) {
        this.cupcakeService = cupcakeService;
    }

    @GetMapping("/admin/cupcakes")
    public String listar(Model model) {
        model.addAttribute("cupcakes", cupcakeService.listarParaAdmin());
        return "admin/cupcakes";
    }

    @GetMapping("/admin/cupcakes/novo")
    public String novo(Model model) {
        model.addAttribute("form", new CupcakeForm());
        model.addAttribute("categorias", cupcakeService.categorias());
        return "admin/cupcake-form";
    }

    @GetMapping("/admin/cupcakes/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            Cupcake cupcake = cupcakeService.buscarParaDetalhe(id);
            model.addAttribute("form", CupcakeForm.de(cupcake));
            model.addAttribute("categorias", cupcakeService.categorias());
            return "admin/cupcake-form";
        } catch (NegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/admin/cupcakes";
        }
    }

    @PostMapping("/admin/cupcakes")
    public String salvar(@ModelAttribute("form") CupcakeForm form, Model model, RedirectAttributes redirect) {
        try {
            cupcakeService.salvar(form);
            redirect.addFlashAttribute("sucesso", "Produto salvo com sucesso");
            return "redirect:/admin/cupcakes";
        } catch (NegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("categorias", cupcakeService.categorias());
            return "admin/cupcake-form";
        }
    }

    @PostMapping("/admin/cupcakes/{id}/remover")
    public String remover(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            cupcakeService.remover(id);
            redirect.addFlashAttribute("sucesso", "Produto removido da vitrine");
        } catch (NegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/cupcakes";
    }
}

package br.com.cupcakes.controller;

import br.com.cupcakes.exception.NegocioException;
import br.com.cupcakes.model.StatusPedido;
import br.com.cupcakes.service.PedidoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HU-14: o administrador vê todos os pedidos e atualiza o status. */
@Controller
public class AdminPedidoController {

    private final PedidoService pedidoService;

    public AdminPedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/admin/pedidos")
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidoService.listarTodos());
        model.addAttribute("statusPossiveis", StatusPedido.values());
        return "admin/pedidos";
    }

    @PostMapping("/admin/pedidos/{id}/status")
    public String atualizarStatus(@PathVariable Long id, @RequestParam StatusPedido status,
                                  RedirectAttributes redirect) {
        try {
            pedidoService.atualizarStatus(id, status);
            redirect.addFlashAttribute("sucesso", "Status do pedido #" + id + " atualizado");
        } catch (NegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/pedidos";
    }
}

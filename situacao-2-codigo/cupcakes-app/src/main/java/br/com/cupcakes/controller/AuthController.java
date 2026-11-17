package br.com.cupcakes.controller;

import br.com.cupcakes.config.UsuarioLogado;
import br.com.cupcakes.exception.NegocioException;
import br.com.cupcakes.model.Perfil;
import br.com.cupcakes.model.Usuario;
import br.com.cupcakes.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HU-01 (cadastro) e HU-02 (login). */
@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String senha,
                        HttpSession sessao, Model model) {
        try {
            Usuario usuario = usuarioService.autenticar(email, senha);
            sessao.setAttribute(UsuarioLogado.ATRIBUTO_SESSAO, new UsuarioLogado(usuario));
            return usuario.getPerfil() == Perfil.ADMIN ? "redirect:/admin/pedidos" : "redirect:/";
        } catch (NegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("email", email);
            return "login";
        }
    }

    @GetMapping("/cadastro")
    public String cadastroForm() {
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@RequestParam String nome, @RequestParam String email,
                            @RequestParam String senha, Model model, RedirectAttributes redirect) {
        try {
            usuarioService.cadastrarCliente(nome, email, senha);
            redirect.addFlashAttribute("sucesso", "Conta criada com sucesso! Faça login para continuar.");
            return "redirect:/login";
        } catch (NegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("nome", nome);
            model.addAttribute("email", email);
            return "cadastro";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession sessao) {
        sessao.invalidate();
        return "redirect:/";
    }
}

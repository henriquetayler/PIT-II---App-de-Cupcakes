package br.com.cupcakes.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession sessao = request.getSession(false);
        UsuarioLogado usuario = sessao == null ? null
                : (UsuarioLogado) sessao.getAttribute(UsuarioLogado.ATRIBUTO_SESSAO);

        String contexto = request.getContextPath();
        if (usuario == null) {
            response.sendRedirect(contexto + "/login");
            return false;
        }
        if (request.getRequestURI().startsWith(contexto + "/admin") && !usuario.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso restrito a administradores");
            return false;
        }
        return true;
    }
}

package br.com.cupcakes.config;

import br.com.cupcakes.model.Perfil;
import br.com.cupcakes.model.Usuario;

import java.io.Serializable;

/** Dados mínimos do usuário guardados na sessão HTTP depois do login. */
public class UsuarioLogado implements Serializable {

    public static final String ATRIBUTO_SESSAO = "usuarioLogado";

    private final Long id;
    private final String nome;
    private final Perfil perfil;

    public UsuarioLogado(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.perfil = usuario.getPerfil();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Perfil getPerfil() { return perfil; }

    public boolean isAdmin() {
        return perfil == Perfil.ADMIN;
    }
}

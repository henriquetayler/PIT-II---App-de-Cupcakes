package br.com.cupcakes.service;

import br.com.cupcakes.exception.NegocioException;
import br.com.cupcakes.model.Perfil;
import br.com.cupcakes.model.Usuario;
import br.com.cupcakes.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** HU-01 (cadastro), HU-02 (login). */
@Service
public class UsuarioService {

    public static final int MAX_TENTATIVAS = 5;        // HU-02 RN#1
    public static final int MINUTOS_BLOQUEIO = 15;     // HU-02 RN#1
    public static final int TAMANHO_MINIMO_SENHA = 8;  // HU-01 RN#2

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrarCliente(String nome, String email, String senha) {
        return cadastrar(nome, email, senha, Perfil.CLIENTE);
    }

    @Transactional
    public Usuario cadastrar(String nome, String email, String senha, Perfil perfil) {
        if (vazio(nome) || vazio(email) || vazio(senha)) {
            throw new NegocioException("Preencha todos os campos obrigatórios");
        }
        String emailNormalizado = email.trim().toLowerCase();
        if (!emailNormalizado.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new NegocioException("E-mail inválido");
        }
        if (senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new NegocioException("A senha deve ter no mínimo " + TAMANHO_MINIMO_SENHA + " caracteres");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new NegocioException("E-mail já cadastrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setPerfil(perfil);
        return usuarioRepository.save(usuario);
    }

    /**
     * Valida e-mail e senha. Após 5 erros seguidos bloqueia o acesso por 15 minutos.
     * noRollbackFor: o contador de falhas precisa ser gravado mesmo quando lançamos a exceção.
     */
    @Transactional(noRollbackFor = NegocioException.class)
    public Usuario autenticar(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .orElseThrow(() -> new NegocioException("Credenciais inválidas"));

        LocalDateTime agora = LocalDateTime.now();
        if (usuario.getBloqueadoAte() != null && usuario.getBloqueadoAte().isAfter(agora)) {
            // INT-10: informa o tempo que realmente falta, e não sempre 15 minutos
            long faltam = Math.max(1, (long) Math.ceil(
                    java.time.Duration.between(agora, usuario.getBloqueadoAte()).getSeconds() / 60.0));
            throw new NegocioException("Acesso bloqueado por excesso de tentativas. Tente novamente em "
                    + faltam + (faltam == 1 ? " minuto." : " minutos."));
        }

        if (!passwordEncoder.matches(senha == null ? "" : senha, usuario.getSenhaHash())) {
            usuario.setTentativasFalhas(usuario.getTentativasFalhas() + 1);
            if (usuario.getTentativasFalhas() >= MAX_TENTATIVAS) {
                usuario.setBloqueadoAte(agora.plusMinutes(MINUTOS_BLOQUEIO));
                usuario.setTentativasFalhas(0);
            }
            usuarioRepository.save(usuario);
            throw new NegocioException("Credenciais inválidas");
        }

        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        return usuarioRepository.save(usuario);
    }

    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}

package br.com.cupcakes.service;

import br.com.cupcakes.exception.NegocioException;
import br.com.cupcakes.model.Perfil;
import br.com.cupcakes.model.Usuario;
import br.com.cupcakes.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final String EMAIL = "ana@teste.com";

    @Mock
    private UsuarioRepository repo;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repo, encoder);
    }

    // ---------- HU-01: cadastro ----------

    @Test
    void cadastroComSucessoGuardaSenhaComHashEEmailEmMinusculas() {
        when(repo.existsByEmailIgnoreCase(EMAIL)).thenReturn(false);
        when(repo.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario u = service.cadastrarCliente("Ana", "Ana@Teste.com", "senha1234");

        assertThat(u.getEmail()).isEqualTo(EMAIL);
        assertThat(u.getSenhaHash()).isNotEqualTo("senha1234");
        assertThat(encoder.matches("senha1234", u.getSenhaHash())).isTrue();
        assertThat(u.getPerfil()).isEqualTo(Perfil.CLIENTE);
    }

    @Test
    void cadastroRejeitaEmailJaCadastrado() {
        when(repo.existsByEmailIgnoreCase(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrarCliente("Ana", EMAIL, "senha1234"))
                .isInstanceOf(NegocioException.class)
                .hasMessage("E-mail já cadastrado");
        verify(repo, never()).save(any());
    }

    @Test
    void cadastroRejeitaSenhaComMenosDe8Caracteres() {
        assertThatThrownBy(() -> service.cadastrarCliente("Ana", EMAIL, "1234567"))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("no mínimo 8");
    }

    @Test
    void cadastroRejeitaCamposVazios() {
        assertThatThrownBy(() -> service.cadastrarCliente("", EMAIL, "senha1234"))
                .isInstanceOf(NegocioException.class);
    }

    // ---------- HU-02: login ----------

    private Usuario usuarioComSenha(String senha) {
        Usuario u = new Usuario();
        u.setEmail(EMAIL);
        u.setSenhaHash(encoder.encode(senha));
        return u;
    }

    @Test
    void loginCorretoZeraTentativasFalhas() {
        Usuario u = usuarioComSenha("senha1234");
        u.setTentativasFalhas(3);
        when(repo.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(u));
        when(repo.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario logado = service.autenticar(EMAIL, "senha1234");

        assertThat(logado.getTentativasFalhas()).isZero();
        assertThat(logado.getBloqueadoAte()).isNull();
    }

    @Test
    void senhaIncorretaMostraCredenciaisInvalidas() {
        Usuario u = usuarioComSenha("senha1234");
        when(repo.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(u));
        when(repo.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> service.autenticar(EMAIL, "errada123"))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Credenciais inválidas");
        assertThat(u.getTentativasFalhas()).isEqualTo(1);
    }

    @Test
    void emailInexistenteMostraCredenciaisInvalidas() {
        when(repo.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.autenticar(EMAIL, "qualquer123"))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    void apos5TentativasIncorretasBloqueiaPor15MinutosMesmoComSenhaCerta() {
        Usuario u = usuarioComSenha("senha1234");
        when(repo.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(u));
        when(repo.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        for (int i = 0; i < UsuarioService.MAX_TENTATIVAS; i++) {
            assertThatThrownBy(() -> service.autenticar(EMAIL, "errada123"))
                    .hasMessage("Credenciais inválidas");
        }

        assertThat(u.getBloqueadoAte()).isAfter(LocalDateTime.now().plusMinutes(14));
        assertThatThrownBy(() -> service.autenticar(EMAIL, "senha1234"))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("bloqueado");
    }
}

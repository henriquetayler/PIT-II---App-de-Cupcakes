package br.com.cupcakes.config;

import br.com.cupcakes.model.Categoria;
import br.com.cupcakes.model.Cupcake;
import br.com.cupcakes.model.Cupom;
import br.com.cupcakes.model.Perfil;
import br.com.cupcakes.repository.CategoriaRepository;
import br.com.cupcakes.repository.CupcakeRepository;
import br.com.cupcakes.repository.CupomRepository;
import br.com.cupcakes.repository.UsuarioRepository;
import br.com.cupcakes.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Na primeira execução cria o administrador, as categorias e alguns cupcakes de exemplo. */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final CategoriaRepository categoriaRepository;
    private final CupcakeRepository cupcakeRepository;
    private final CupomRepository cupomRepository;
    private final String adminEmail;
    private final String adminSenha;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           UsuarioService usuarioService,
                           CategoriaRepository categoriaRepository,
                           CupcakeRepository cupcakeRepository,
                           CupomRepository cupomRepository,
                           @Value("${loja.admin.email}") String adminEmail,
                           @Value("${loja.admin.senha}") String adminSenha) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.categoriaRepository = categoriaRepository;
        this.cupcakeRepository = cupcakeRepository;
        this.cupomRepository = cupomRepository;
        this.adminEmail = adminEmail;
        this.adminSenha = adminSenha;
    }

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByEmailIgnoreCase(adminEmail)) {
            usuarioService.cadastrar("Administrador", adminEmail, adminSenha, Perfil.ADMIN);
        }
        if (categoriaRepository.count() == 0) {
            List<Categoria> categorias = categoriaRepository.saveAll(List.of(
                    new Categoria("Chocolate"), new Categoria("Frutas"),
                    new Categoria("Tradicional"), new Categoria("Aniversário"),
                    new Categoria("Presente")));
            if (cupcakeRepository.count() == 0) {
                criar("Brigadeiro Belga", "Massa de chocolate com cobertura de brigadeiro belga.",
                        "9.50", 30, "Leite, glúten, ovos", Set.of(categorias.get(0), categorias.get(3)));
                criar("Morango com Chantilly", "Massa de baunilha, recheio de geleia de morango e chantilly.",
                        "10.00", 25, "Leite, glúten, ovos", Set.of(categorias.get(1), categorias.get(4)));
                criar("Limão Siciliano", "Massa cítrica com cobertura de cream cheese.",
                        "9.00", 20, "Leite, glúten, ovos", Set.of(categorias.get(1)));
                criar("Baunilha Clássico", "O tradicional: baunilha com buttercream.",
                        "7.50", 40, "Leite, glúten, ovos", Set.of(categorias.get(2), categorias.get(3)));
                criar("Red Velvet", "Massa aveludada vermelha com cream cheese.",
                        "11.00", 15, "Leite, glúten, ovos", Set.of(categorias.get(2), categorias.get(4)));
                criar("Ninho com Nutella", "Massa branca, recheio de Nutella e cobertura de leite Ninho.",
                        "12.00", 18, "Leite, glúten, ovos, avelã", Set.of(categorias.get(0), categorias.get(4)));
            }
        }
        // HU-11: cupons de exemplo (dois válidos e um vencido, para testar as duas mensagens)
        if (cupomRepository.count() == 0) {
            cupomRepository.save(new Cupom("BEMVINDO10", 10, LocalDate.now().plusYears(1), 100));
            cupomRepository.save(new Cupom("DOCE15", 15, LocalDate.now().plusMonths(3), 50));
            cupomRepository.save(new Cupom("NATAL2025", 20, LocalDate.of(2025, 12, 31), 100));
        }
    }

    private void criar(String nome, String descricao, String preco, int estoque,
                       String alergenicos, Set<Categoria> categorias) {
        Cupcake c = new Cupcake();
        c.setNome(nome);
        c.setDescricao(descricao);
        c.setPreco(new BigDecimal(preco));
        c.setEstoque(estoque);
        c.setAlergenicos(alergenicos);
        c.setCategorias(new java.util.LinkedHashSet<>(categorias));
        cupcakeRepository.save(c);
    }
}

package br.com.cupcakes.service;

import br.com.cupcakes.dto.CupcakeForm;
import br.com.cupcakes.exception.NegocioException;
import br.com.cupcakes.model.Categoria;
import br.com.cupcakes.model.Cupcake;
import br.com.cupcakes.repository.CategoriaRepository;
import br.com.cupcakes.repository.CupcakeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

/** HU-03/04/05 (vitrine), HU-12 (catálogo) e HU-13 (estoque). */
@Service
public class CupcakeService {

    private final CupcakeRepository cupcakeRepository;
    private final CategoriaRepository categoriaRepository;

    public CupcakeService(CupcakeRepository cupcakeRepository, CategoriaRepository categoriaRepository) {
        this.cupcakeRepository = cupcakeRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Cupcake> vitrine(Long categoriaId) {
        if (categoriaId == null) {
            return cupcakeRepository.findByAtivoTrueAndEstoqueGreaterThanOrderByNome(0);
        }
        return cupcakeRepository.vitrinePorCategoria(categoriaId);
    }

    public List<Categoria> categorias() {
        return categoriaRepository.findAllByOrderByNomeAsc();
    }

    public Cupcake buscarParaDetalhe(Long id) {
        return cupcakeRepository.findById(id)
                .filter(Cupcake::isAtivo)
                .orElseThrow(() -> new NegocioException("Produto não encontrado"));
    }

    public List<Cupcake> listarParaAdmin() {
        return cupcakeRepository.findByAtivoTrueOrderByNome();
    }

    @Transactional
    public Cupcake salvar(CupcakeForm form) {
        validar(form);
        Cupcake cupcake = form.getId() == null
                ? new Cupcake()
                : cupcakeRepository.findById(form.getId())
                        .orElseThrow(() -> new NegocioException("Produto não encontrado"));

        cupcake.setNome(form.getNome().trim());
        cupcake.setDescricao(form.getDescricao().trim());
        cupcake.setPreco(form.getPreco());
        cupcake.setFotoUrl(form.getFotoUrl() == null || form.getFotoUrl().isBlank() ? null : form.getFotoUrl().trim());
        cupcake.setAlergenicos(form.getAlergenicos() == null || form.getAlergenicos().isBlank()
                ? null : form.getAlergenicos().trim());
        cupcake.setEstoque(form.getEstoque());
        cupcake.setAtivo(true);
        cupcake.setCategorias(new LinkedHashSet<>(categoriaRepository.findAllById(form.getCategoriaIds())));
        return cupcakeRepository.save(cupcake);
    }

    /** HU-12 CA#2: remove da vitrine, mas mantém o registro para o histórico de pedidos. */
    @Transactional
    public void remover(Long id) {
        Cupcake cupcake = cupcakeRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Produto não encontrado"));
        cupcake.setAtivo(false);
        cupcakeRepository.save(cupcake);
    }

    private void validar(CupcakeForm f) {
        // HU-12 RN#1: nome, categoria, preço, foto e descrição são obrigatórios.
        // A foto é opcional neste MVP (ver README); as demais são exigidas.
        if (f.getNome() == null || f.getNome().isBlank()
                || f.getDescricao() == null || f.getDescricao().isBlank()
                || f.getPreco() == null || f.getEstoque() == null) {
            throw new NegocioException("Preencha nome, descrição, preço e estoque");
        }
        if (f.getCategoriaIds().isEmpty()) {
            throw new NegocioException("Selecione ao menos uma categoria");
        }
        // HU-12 RN#2
        if (f.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("O preço deve ser maior que zero");
        }
        if (f.getEstoque() < 0) {
            throw new NegocioException("O estoque não pode ser negativo");
        }
    }
}

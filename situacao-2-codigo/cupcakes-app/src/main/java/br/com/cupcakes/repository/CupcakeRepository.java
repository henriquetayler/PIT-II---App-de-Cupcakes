package br.com.cupcakes.repository;

import br.com.cupcakes.model.Cupcake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CupcakeRepository extends JpaRepository<Cupcake, Long> {

    /** Vitrine: somente ativos e com estoque (HU-03 RN#1). */
    List<Cupcake> findByAtivoTrueAndEstoqueGreaterThanOrderByNome(int estoqueMinimo);

    /** Vitrine filtrada por categoria (HU-04). */
    @Query("select distinct c from Cupcake c join c.categorias cat "
            + "where c.ativo = true and c.estoque > 0 and cat.id = :categoriaId order by c.nome")
    List<Cupcake> vitrinePorCategoria(@Param("categoriaId") Long categoriaId);

    /** Painel do administrador: tudo que não foi removido. */
    List<Cupcake> findByAtivoTrueOrderByNome();
}

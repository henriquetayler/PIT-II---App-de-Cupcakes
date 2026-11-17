package br.com.cupcakes.dto;

import br.com.cupcakes.model.Cupcake;
import br.com.cupcakes.model.Categoria;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Dados do formulário de cadastro/edição de cupcake (área do administrador). */
public class CupcakeForm {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private String fotoUrl;
    private String alergenicos;
    private Integer estoque;
    private List<Long> categoriaIds = new ArrayList<>();

    public static CupcakeForm de(Cupcake c) {
        CupcakeForm f = new CupcakeForm();
        f.id = c.getId();
        f.nome = c.getNome();
        f.descricao = c.getDescricao();
        f.preco = c.getPreco();
        f.fotoUrl = c.getFotoUrl();
        f.alergenicos = c.getAlergenicos();
        f.estoque = c.getEstoque();
        for (Categoria cat : c.getCategorias()) {
            f.categoriaIds.add(cat.getId());
        }
        return f;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getAlergenicos() { return alergenicos; }
    public void setAlergenicos(String alergenicos) { this.alergenicos = alergenicos; }

    public Integer getEstoque() { return estoque; }
    public void setEstoque(Integer estoque) { this.estoque = estoque; }

    public List<Long> getCategoriaIds() { return categoriaIds; }
    public void setCategoriaIds(List<Long> categoriaIds) {
        this.categoriaIds = categoriaIds == null ? new ArrayList<>() : categoriaIds;
    }
}

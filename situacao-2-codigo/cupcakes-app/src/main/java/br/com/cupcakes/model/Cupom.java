package br.com.cupcakes.model;

import jakarta.persistence.*;

import java.time.LocalDate;

/** HU-11: cupom de desconto percentual com validade e limite de uso. */
@Entity
@Table(name = "cupom")
public class Cupom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código digitado pelo cliente, sempre em maiúsculas. */
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    /** Desconto em % sobre o subtotal (1 a 100). */
    @Column(nullable = false)
    private int percentual;

    /** Último dia em que o cupom vale (HU-11 RN#2). */
    @Column(nullable = false)
    private LocalDate validade;

    /** Quantas vezes o cupom pode ser usado no total (HU-11 RN#2). */
    @Column(name = "limite_uso", nullable = false)
    private int limiteUso;

    @Column(nullable = false)
    private int usos;

    @Column(nullable = false)
    private boolean ativo = true;

    public Cupom() {
    }

    public Cupom(String codigo, int percentual, LocalDate validade, int limiteUso) {
        this.codigo = codigo;
        this.percentual = percentual;
        this.validade = validade;
        this.limiteUso = limiteUso;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public int getPercentual() { return percentual; }
    public void setPercentual(int percentual) { this.percentual = percentual; }
    public LocalDate getValidade() { return validade; }
    public void setValidade(LocalDate validade) { this.validade = validade; }
    public int getLimiteUso() { return limiteUso; }
    public void setLimiteUso(int limiteUso) { this.limiteUso = limiteUso; }
    public int getUsos() { return usos; }
    public void setUsos(int usos) { this.usos = usos; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}

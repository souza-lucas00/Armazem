package br.com.armazem.entity;

import br.com.armazem.enums.TipoIngrediente;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "compartimentos")
public class Compartimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    private TipoIngrediente tipoAtual;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidadeAtual = BigDecimal.ZERO;

    private LocalDate dataUltimaUtilizacao;

    @Version
    private Long versao;

    public Compartimento() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public TipoIngrediente getTipoAtual() {
        return tipoAtual;
    }

    public void setTipoAtual(TipoIngrediente tipoAtual) {
        this.tipoAtual = tipoAtual;
    }

    public BigDecimal getQuantidadeAtual() {
        return quantidadeAtual;
    }

    public void setQuantidadeAtual(BigDecimal quantidadeAtual) {
        this.quantidadeAtual = quantidadeAtual;
    }

    public LocalDate getDataUltimaUtilizacao() {
        return dataUltimaUtilizacao;
    }

    public void setDataUltimaUtilizacao(LocalDate dataUltimaUtilizacao) {
        this.dataUltimaUtilizacao = dataUltimaUtilizacao;
    }

    public Long getVersao() {
        return versao;
    }

    public void setVersao(Long versao) {
        this.versao = versao;
    }

    // Getters e setters
}
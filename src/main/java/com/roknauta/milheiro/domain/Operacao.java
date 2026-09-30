package com.roknauta.milheiro.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Atributos comuns dos lançamentos efetivos; parâmetros de simulação ficam no formulário. */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class Operacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    private Long versao;
    @Column(unique = true, length = 64)
    private String chaveImportacao;
    @Column(nullable = false)
    private LocalDate data = LocalDate.now();
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter(lombok.AccessLevel.PROTECTED)
    private TipoOperacao tipo;
    @Enumerated(EnumType.STRING)
    private StatusOperacao status = StatusOperacao.CONFIRMADO;
    @ManyToOne(optional = false)
    private Programa programa;
    @Column(nullable = false, precision = 24, scale = 2)
    private BigDecimal quantidade = BigDecimal.ZERO;
    @Column(nullable = false, precision = 24, scale = 2)
    private BigDecimal valor = BigDecimal.ZERO;
    // Nullable para identificar registros anteriores à migração.
    @Column(precision = 24, scale = 2)
    private BigDecimal desembolso = BigDecimal.ZERO;
    @Column(length = 500)
    private String observacoes;

    public boolean isConfirmada() {
        return status == StatusOperacao.CONFIRMADO;
    }

    public boolean isCancelada() {
        return status == StatusOperacao.CANCELADO;
    }

    public boolean isTransferencia() {
        return tipo == TipoOperacao.TRANSFERENCIA;
    }

    public boolean isCreditoTransferencia() {
        return getTransferenciaOrigem() != null;
    }

    public Programa getDestino() {
        return null;
    }

    public Operacao getTransferenciaOrigem() {
        return null;
    }

    public ParcelaTransferencia getParcelaTransferencia() {
        return null;
    }

    public Long getVinculoTransferencia() {
        return isCreditoTransferencia() ? getTransferenciaOrigem().getId() : (isTransferencia() ? id : null);
    }

    public String getParcelaDescricao() {
        return getParcelaTransferencia() == null ? "" : getParcelaTransferencia().getDescricao();
    }

    public BigDecimal getDesembolsoEfetivo() {
        return isConfirmada() ? desembolso : BigDecimal.ZERO;
    }

    public BigDecimal getReceitaEfetiva() {
        return isConfirmada() && tipo == TipoOperacao.VENDA ? valor : BigDecimal.ZERO;
    }

    public BigDecimal getMilheiro() {
        return quantidade.signum() == 0
            ? BigDecimal.ZERO
            : valor.multiply(new BigDecimal("1000")).divide(quantidade, 4, java.math.RoundingMode.HALF_UP);
    }
}

package com.roknauta.milheiro.domain;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Atributos comuns dos lançamentos efetivos; parâmetros de simulação ficam no formulário. */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 31)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class Operacao extends EntidadeBase {

    // Protege alterações de status e estornos contra atualizações concorrentes.
    @Version
    private Long versao;
    @Column(nullable = false)
    @Builder.Default
    private LocalDate data = LocalDate.now();
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusOperacao status = StatusOperacao.CONFIRMADO;
    @ManyToOne(optional = false)
    private ProgramaFidelidade programa;
    @Column(nullable = false, precision = 24, scale = 2)
    @Builder.Default
    private BigDecimal quantidade = BigDecimal.ZERO;
    @Column(nullable = false, precision = 24, scale = 2)
    @Builder.Default
    @Convert(converter = com.roknauta.milheiro.persistence.DinheiroPersistenceConverter.class)
    private Dinheiro valor = Dinheiro.ZERO;
    @Column(length = 500)
    private String observacoes;

    /** O tipo é determinado pela entidade concreta, sem estado duplicado. */
    @Transient
    public abstract TipoOperacao getTipo();

    public boolean isConfirmada() {
        return status == StatusOperacao.CONFIRMADO;
    }

    public boolean isCancelada() {
        return status == StatusOperacao.CANCELADO;
    }

    public boolean isTransferencia() {
        return getTipo() == TipoOperacao.TRANSFERENCIA;
    }

    public boolean isCreditoTransferencia() {
        return getTransferenciaOrigem() != null;
    }

    public ProgramaFidelidade getDestino() {
        return null;
    }

    public Operacao getTransferenciaOrigem() {
        return null;
    }

    public ParcelaTransferencia getParcelaTransferencia() {
        return null;
    }

    public Long getVinculoTransferencia() {
        return isCreditoTransferencia() ? getTransferenciaOrigem().getId() : (isTransferencia() ? getId() : null);
    }

    public String getParcelaDescricao() {
        return getParcelaTransferencia() == null ? "" : getParcelaTransferencia().getDescricao();
    }

    public Dinheiro getDesembolsoEfetivo() {
        return Dinheiro.de(BigDecimal.ZERO);
    }

    public Dinheiro getReceitaEfetiva() {
        return Dinheiro.de(isConfirmada() && getTipo() == TipoOperacao.VENDA ? valor : BigDecimal.ZERO);
    }

    public Dinheiro getMilheiro() {
        return Dinheiro.de(quantidade.signum() == 0
            ? BigDecimal.ZERO
            : valor.multiply(new BigDecimal("1000")).divide(quantidade, 4, java.math.RoundingMode.HALF_UP));
    }
}

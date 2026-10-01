package com.roknauta.milheiro.domain;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("ACUMULO")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Acumulo extends Operacao {
    @ManyToOne
    @JoinColumn(name = "transferencia_origem_id")
    private Transferencia transferenciaOrigem;
    @Enumerated(EnumType.STRING)
    private ParcelaTransferencia parcelaTransferencia;

    @Override
    public TipoOperacao getTipo() {
        return TipoOperacao.ACUMULO;
    }
    @Override
    public Dinheiro getDesembolsoEfetivo() {
        return Dinheiro.de(isConfirmada() && !isCreditoTransferencia() ? getValor() : java.math.BigDecimal.ZERO);
    }
}

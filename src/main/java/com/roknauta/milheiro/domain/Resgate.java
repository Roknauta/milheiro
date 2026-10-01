package com.roknauta.milheiro.domain;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("RESGATE")
@SuperBuilder
@NoArgsConstructor
public class Resgate extends Operacao {

    @Override
    public TipoOperacao getTipo() {
        return TipoOperacao.RESGATE;
    }
    @Override
    public Dinheiro getDesembolsoEfetivo() {
        return Dinheiro.de(isConfirmada() ? getValor() : java.math.BigDecimal.ZERO);
    }
}

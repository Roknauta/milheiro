package com.roknauta.milheiro.domain;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("VENDA")
@SuperBuilder
@NoArgsConstructor
public class Venda extends Operacao {
    @Override
    public TipoOperacao getTipo() {
        return TipoOperacao.VENDA;
    }
}

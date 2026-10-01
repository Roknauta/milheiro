package com.roknauta.milheiro.domain;

import lombok.Builder;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("TRANSFERENCIA")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Transferencia extends Operacao {
    @Column(precision = 24, scale = 2)
    @Builder.Default
    @Convert(converter = com.roknauta.milheiro.persistence.DinheiroPersistenceConverter.class)
    private Dinheiro valorAdicional = Dinheiro.ZERO;

    @ManyToOne(optional = false)
    private ProgramaFidelidade destino;

    @Override
    public TipoOperacao getTipo() {
        return TipoOperacao.TRANSFERENCIA;
    }
    @Override
    public Dinheiro getDesembolsoEfetivo() {
        return Dinheiro.de(isConfirmada() ? valorAdicional : java.math.BigDecimal.ZERO);
    }
}

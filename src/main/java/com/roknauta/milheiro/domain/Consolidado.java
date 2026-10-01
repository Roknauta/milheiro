package com.roknauta.milheiro.domain;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Consolidado extends EntidadeBase {

    // A identidade herdada é derivada do programa pelo mapeamento @MapsId.
    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(name = "programa_id")
    private ProgramaFidelidade programa;
    @Column(nullable = false, precision = 24, scale = 2)
    @Builder.Default
    private BigDecimal acumulado = BigDecimal.ZERO;
    @Column(nullable = false, precision = 24, scale = 2)
    @Builder.Default
    private BigDecimal saldo = BigDecimal.ZERO;
    @Column(nullable = false, precision = 38, scale = 12)
    @Builder.Default
    @Convert(converter = com.roknauta.milheiro.persistence.DinheiroPersistenceConverter.class)
    private Dinheiro milheiro = Dinheiro.ZERO;
}

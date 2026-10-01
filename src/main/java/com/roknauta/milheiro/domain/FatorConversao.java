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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"origem_id", "destino_id"}))
@SuperBuilder
@NoArgsConstructor
public class FatorConversao extends EntidadeBase {

    @ManyToOne(optional = false)
    private ProgramaFidelidade origem;
    @ManyToOne(optional = false)
    private ProgramaFidelidade destino;
    @Column(nullable = false, precision = 24, scale = 10)
    @Builder.Default
    private BigDecimal pontosOrigem = BigDecimal.ONE;
    @Column(nullable = false, precision = 24, scale = 10)
    @Builder.Default
    private BigDecimal pontosDestino = BigDecimal.ONE;
}

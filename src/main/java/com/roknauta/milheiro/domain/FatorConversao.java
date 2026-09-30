package com.roknauta.milheiro.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"origem_id", "destino_id"}))
public class FatorConversao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Programa origem;
    @ManyToOne(optional = false)
    private Programa destino;
    @Column(nullable = false, precision = 24, scale = 10)
    private BigDecimal pontosOrigem = BigDecimal.ONE;
    @Column(nullable = false, precision = 24, scale = 10)
    private BigDecimal pontosDestino = BigDecimal.ONE;
}

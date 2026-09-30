package com.roknauta.milheiro.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Consolidado {

    @Id
    private Long id;
    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(name = "programa_id")
    private Programa programa;
    @Column(nullable = false, precision = 24, scale = 2)
    private BigDecimal acumulado = BigDecimal.ZERO;
    @Column(nullable = false, precision = 24, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;
    @Column(nullable = false, precision = 38, scale = 12)
    private BigDecimal milheiro = BigDecimal.ZERO;
}

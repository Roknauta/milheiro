package com.roknauta.milheiro.domain;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/** Identidade comum das entidades; associações com @MapsId derivam a chave da entidade vinculada. */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class EntidadeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}

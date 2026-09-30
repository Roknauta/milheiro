package com.roknauta.milheiro.domain;

import jakarta.persistence.Entity;

@Entity
public class Resgate extends Operacao {

    public Resgate() {
        setTipo(TipoOperacao.RESGATE);
    }
}

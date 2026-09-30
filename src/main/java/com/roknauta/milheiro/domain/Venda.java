package com.roknauta.milheiro.domain;

import jakarta.persistence.Entity;

@Entity
public class Venda extends Operacao {
    public Venda() { setTipo(TipoOperacao.VENDA); }
}

package com.roknauta.milheiro.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Transferencia extends Operacao {
    @ManyToOne(optional = false)
    private Programa destino;

    public Transferencia() { setTipo(TipoOperacao.TRANSFERENCIA); }
}

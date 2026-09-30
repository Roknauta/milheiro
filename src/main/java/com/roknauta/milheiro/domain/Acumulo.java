package com.roknauta.milheiro.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Acumulo extends Operacao {
    @ManyToOne
    @JoinColumn(name = "transferencia_origem_id")
    private Transferencia transferenciaOrigem;
    @Enumerated(EnumType.STRING)
    private ParcelaTransferencia parcelaTransferencia;

    public Acumulo() { setTipo(TipoOperacao.ACUMULO); }
}

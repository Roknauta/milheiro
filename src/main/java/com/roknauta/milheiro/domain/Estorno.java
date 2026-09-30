package com.roknauta.milheiro.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Estorno extends Operacao {
    @ManyToOne(optional = false)
    private Operacao operacaoOriginal;

    public Estorno() { setTipo(TipoOperacao.ESTORNO); }
    @Override
    public BigDecimal getDesembolsoEfetivo() {
        return isConfirmada() ? getDesembolso().negate() : BigDecimal.ZERO;
    }
    @Override
    public BigDecimal getReceitaEfetiva() {
        return isConfirmada() && operacaoOriginal.getTipo() == TipoOperacao.VENDA
            ? getValor().negate() : BigDecimal.ZERO;
    }
}

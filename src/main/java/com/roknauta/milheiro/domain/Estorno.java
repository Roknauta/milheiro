package com.roknauta.milheiro.domain;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("ESTORNO")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Estorno extends Operacao {
    @ManyToOne(optional = false)
    private Operacao operacaoOriginal;

    @Override
    public TipoOperacao getTipo() {
        return TipoOperacao.ESTORNO;
    }
    @Override
    public Dinheiro getDesembolsoEfetivo() {
        return Dinheiro.de(isConfirmada() ? operacaoOriginal.getDesembolsoEfetivo().negate() : BigDecimal.ZERO);
    }
    @Override
    public Dinheiro getReceitaEfetiva() {
        return Dinheiro.de(isConfirmada() && operacaoOriginal.getTipo() == TipoOperacao.VENDA
            ? getValor().negate() : BigDecimal.ZERO);
    }
}

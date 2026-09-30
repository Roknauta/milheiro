package com.roknauta.milheiro.service;

import com.roknauta.milheiro.domain.Programa;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Resumo {

    private final Programa programa;
    private BigDecimal acumulado = BigDecimal.ZERO;
    private BigDecimal saldo = BigDecimal.ZERO;
    private BigDecimal gasto = BigDecimal.ZERO;
    private BigDecimal vendas = BigDecimal.ZERO;

    public Resumo(Programa programa) {
        this.programa = programa;
    }

    public BigDecimal getMilheiro() {
        return acumulado.signum() == 0 ? BigDecimal.ZERO : Calculos.milheiro(acumulado, gasto);
    }

    public BigDecimal getResultado() {
        return vendas.subtract(gasto);
    }

    public BigDecimal getCustoEuro() {
        return getMilheiro().divide(new BigDecimal("20"), 6, java.math.RoundingMode.HALF_UP);
    }

    void entrada(BigDecimal pontos, BigDecimal custo) {
        acumulado = acumulado.add(pontos);
        saldo = saldo.add(pontos);
        gasto = gasto.add(custo);
    }

    void saida(BigDecimal pontos) {
        saldo = saldo.subtract(pontos);
        if (saldo.signum() < 0)
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get(
                "interface.saldo.insuficiente.em") + " " + programa.getNome() + com.roknauta.milheiro.web.Textos.get(
                "interface.confira.a.data.e.as.operacoes.posteriores"));
    }
}

package com.roknauta.milheiro.service;

import com.roknauta.milheiro.domain.Dinheiro;
import com.roknauta.milheiro.domain.ProgramaFidelidade;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Resumo {

    private final ProgramaFidelidade programa;
    private BigDecimal acumulado = BigDecimal.ZERO;
    private BigDecimal saldo = BigDecimal.ZERO;
    private Dinheiro gasto = Dinheiro.ZERO;
    private Dinheiro vendas = Dinheiro.ZERO;

    public Resumo(ProgramaFidelidade programa) {
        this.programa = programa;
    }

    public Dinheiro getMilheiro() {
        return Dinheiro.de(acumulado.signum() == 0 ? BigDecimal.ZERO : Calculos.milheiro(acumulado, gasto));
    }

    public Dinheiro getResultado() {
        return Dinheiro.de(vendas.subtract(gasto));
    }

    public Dinheiro getCustoEuro() {
        return Dinheiro.de(getMilheiro().divide(new BigDecimal("20"), 6, java.math.RoundingMode.HALF_UP));
    }

    void entrada(BigDecimal pontos, BigDecimal custo) {
        acumulado = acumulado.add(pontos);
        saldo = saldo.add(pontos);
        gasto = Dinheiro.de(gasto.add(custo));
    }

    void saida(BigDecimal pontos) {
        saldo = saldo.subtract(pontos);
        if (saldo.signum() < 0)
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get(
                "interface.saldo.insuficiente.em") + " " + programa.getNome() + com.roknauta.milheiro.web.Textos.get(
                "interface.confira.a.data.e.as.operacoes.posteriores"));
    }
    public void setGasto(BigDecimal valor) {
        this.gasto = Dinheiro.de(valor);
    }
    public void setVendas(BigDecimal valor) {
        this.vendas = Dinheiro.de(valor);
    }
}

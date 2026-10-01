package com.roknauta.milheiro.web;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.service.OperacaoService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@Component("dashboardController")
@Scope("view")
public class DashboardController implements Serializable {
    private final OperacaoService service;
    private List<Consolidado> consolidados = List.of();
    private List<Operacao> operacoes = List.of();

    public DashboardController(OperacaoService service) {
        this.service = service;
    }

    @PostConstruct
    public void carregar() {
        operacoes = service.operacoes();
        consolidados = service.consolidados();
    }

    public List<Consolidado> getConsolidados() {
        return consolidados.stream()
            .sorted(Comparator.comparing(c -> c.getPrograma().getNome(), String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    public BigDecimal getSaldoTotal() {
        return consolidados.stream().map(Consolidado::getSaldo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Dinheiro getDesembolso() {
        return Dinheiro.de(operacoes.stream().<BigDecimal>map(Operacao::getDesembolsoEfetivo).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public Dinheiro getReceita() {
        return Dinheiro.de(operacoes.stream().<BigDecimal>map(Operacao::getReceitaEfetiva).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public Dinheiro getResultado() {
        return Dinheiro.de(getReceita().subtract(getDesembolso()));
    }

    public String getGraficoPontos() {
        return graficoSaldos(CategoriaProgramaFidelidade.PONTOS);
    }

    public String getGraficoMilhas() {
        return graficoSaldos(CategoriaProgramaFidelidade.MILHAS);
    }

    public boolean isTemSaldoPontos() {
        return !saldosDaCategoria(CategoriaProgramaFidelidade.PONTOS).isEmpty();
    }

    public boolean isTemSaldoMilhas() {
        return !saldosDaCategoria(CategoriaProgramaFidelidade.MILHAS).isEmpty();
    }

    private List<Consolidado> saldosDaCategoria(CategoriaProgramaFidelidade categoria) {
        return consolidados.stream()
            .filter(c -> c.getPrograma().getCategoria() == categoria && c.getSaldo().signum() > 0)
            .sorted(Comparator.comparing(c -> c.getPrograma().getNome(), String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private String graficoSaldos(CategoriaProgramaFidelidade categoria) {
        return com.roknauta.milheiro.helper.GraficoSaldoHelper.pizza(saldosDaCategoria(categoria));
    }
}

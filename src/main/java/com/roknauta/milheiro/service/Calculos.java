package com.roknauta.milheiro.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Calculos {

    public static final BigDecimal MIL = new BigDecimal("1000");

    /** Equivalência proporcional: 2.000 pontos ALL correspondem a 40 euros. */
    public static BigDecimal eurosAll(BigDecimal pontos) {
        return pontos.multiply(new BigDecimal("40")).divide(new BigDecimal("2000"));
    }

    private Calculos() {
    }

    public static void positivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0)
            throw new IllegalArgumentException(campo + " " + com.roknauta.milheiro.web.Textos.get("interface.deve.ser.maior.que.zero"));
    }

    public static void naoNegativo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() < 0)
            throw new IllegalArgumentException(campo + " " + com.roknauta.milheiro.web.Textos.get("interface.nao.pode.ser.negativo"));
    }

    public static BigDecimal dividir(BigDecimal a, BigDecimal b) {
        positivo(b, com.roknauta.milheiro.web.Textos.get("campo.divisor"));
        return a.divide(b, 12, RoundingMode.HALF_UP);
    }

    public static BigDecimal multiplicador(BigDecimal fator, BigDecimal bonus) {
        positivo(fator, com.roknauta.milheiro.web.Textos.get("campo.fator"));
        naoNegativo(bonus, com.roknauta.milheiro.web.Textos.get("interface.bonus.2"));
        return fator.multiply(BigDecimal.ONE.add(bonus.movePointLeft(2)));
    }

    public static BigDecimal transferir(BigDecimal pontos, BigDecimal fator, BigDecimal bonus) {
        positivo(pontos, com.roknauta.milheiro.web.Textos.get("campo.pontos"));
        return pontos.multiply(multiplicador(fator, bonus));
    }

    public static BigDecimal necessarios(BigDecimal desejados, BigDecimal fator, BigDecimal bonus) {
        positivo(desejados, com.roknauta.milheiro.web.Textos.get("interface.pontos.desejados"));
        return desejados.divide(multiplicador(fator, bonus), 0, RoundingMode.CEILING);
    }

    public static BigDecimal transferirProporcao(BigDecimal pontos, BigDecimal origem, BigDecimal destino,
        BigDecimal bonus, int escala, RoundingMode arredondamento) {
        positivo(pontos, com.roknauta.milheiro.web.Textos.get("campo.pontos"));
        positivo(origem, com.roknauta.milheiro.web.Textos.get("interface.proporcao.de.origem"));
        positivo(destino, com.roknauta.milheiro.web.Textos.get("interface.proporcao.de.destino"));
        naoNegativo(bonus, com.roknauta.milheiro.web.Textos.get("interface.bonus.2"));
        return pontos.multiply(destino).multiply(BigDecimal.ONE.add(bonus.movePointLeft(2)))
            .divide(origem, escala, arredondamento);
    }

    public static BigDecimal necessariosProporcao(BigDecimal desejados, BigDecimal origem, BigDecimal destino,
        BigDecimal bonus) {
        positivo(desejados, com.roknauta.milheiro.web.Textos.get("interface.pontos.desejados"));
        positivo(origem, com.roknauta.milheiro.web.Textos.get("interface.proporcao.de.origem"));
        return desejados.multiply(origem).divide(multiplicador(destino, bonus), 0, RoundingMode.CEILING);
    }

    public static BigDecimal milheiro(BigDecimal pontos, BigDecimal custo) {
        positivo(pontos, com.roknauta.milheiro.web.Textos.get("campo.pontos"));
        naoNegativo(custo, com.roknauta.milheiro.web.Textos.get("campo.custo"));
        return dividir(custo.multiply(MIL), pontos);
    }

    public static BigDecimal proporcional(BigDecimal pontos, BigDecimal acumulado, BigDecimal gasto) {
        naoNegativo(pontos, com.roknauta.milheiro.web.Textos.get("campo.pontos"));
        naoNegativo(gasto, com.roknauta.milheiro.web.Textos.get("campo.gasto"));
        return dividir(pontos.multiply(gasto), acumulado);
    }

    public static BigDecimal venda(BigDecimal pontos, BigDecimal milheiro, BigDecimal taxas) {
        positivo(pontos, com.roknauta.milheiro.web.Textos.get("campo.pontos"));
        naoNegativo(milheiro, com.roknauta.milheiro.web.Textos.get("campo.milheiro"));
        naoNegativo(taxas, com.roknauta.milheiro.web.Textos.get("campo.taxas"));
        return pontos.multiply(milheiro).movePointLeft(3).add(taxas);
    }
}

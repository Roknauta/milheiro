package com.roknauta.milheiro.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/** Valor decimal com apresentação monetária. Formatar não converte a moeda nem aplica câmbio. */
public final class Dinheiro extends BigDecimal {
    public static final Dinheiro ZERO = new Dinheiro(0L);

    public static Dinheiro de(BigDecimal valor) {
        return valor == null ? null : valor instanceof Dinheiro ? (Dinheiro) valor : new Dinheiro(valor);
    }

    private static final long serialVersionUID = 1L;
    private static final Locale PORTUGUES_BRASIL = Locale.forLanguageTag("pt-BR");

    public Dinheiro(String valor) {
        super(valor);
    }

    public Dinheiro(BigDecimal valor) {
        super(Objects.requireNonNull(valor, "valor").unscaledValue(), valor.scale());
    }

    public Dinheiro(long valor) {
        super(valor);
    }

    public String formatarReais() {
        return formatar("BRL", PORTUGUES_BRASIL);
    }

    public String formatarEuros() {
        return formatar("EUR", PORTUGUES_BRASIL);
    }

    public String formatar(String codigoMoeda) {
        return formatar(codigoMoeda, PORTUGUES_BRASIL);
    }

    public String formatar(String codigoMoeda, Locale localidade) {
        return formatar(Currency.getInstance(codigoMoeda), localidade);
    }

    public String formatar(Currency moeda, Locale localidade) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(localidade);
        formato.setCurrency(moeda);
        int casas = moeda.getDefaultFractionDigits();
        if (casas >= 0) {
            formato.setMinimumFractionDigits(casas);
            formato.setMaximumFractionDigits(casas);
        }
        formato.setRoundingMode(RoundingMode.HALF_UP);
        return formato.format(this);
    }
}

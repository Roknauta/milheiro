package com.roknauta.milheiro.web.converter;

import jakarta.faces.convert.NumberConverter;
import jakarta.faces.convert.FacesConverter;
import java.util.Locale;

/** Formatação padronizada de exibição; não altera o valor do modelo. */
@FacesConverter("quantidadeConverter")
public class QuantidadeConverter extends NumberConverter {
    public QuantidadeConverter() {
        setLocale(Locale.forLanguageTag("pt-BR"));
        setGroupingUsed(true);
        setMinFractionDigits(0);
        setMaxFractionDigits(0);
    }
}

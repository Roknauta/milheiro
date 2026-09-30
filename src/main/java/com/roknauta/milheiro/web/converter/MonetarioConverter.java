package com.roknauta.milheiro.web.converter;

import jakarta.faces.convert.NumberConverter;
import jakarta.faces.convert.FacesConverter;
import java.util.Locale;

/** Formatação padronizada de exibição; não altera o valor do modelo. */
@FacesConverter("monetarioConverter")
public class MonetarioConverter extends NumberConverter {
    public MonetarioConverter() {
        setLocale(Locale.forLanguageTag("pt-BR"));
        setGroupingUsed(true);
        setType("currency");
        setCurrencySymbol("R$");
        setMinFractionDigits(2);
        setMaxFractionDigits(2);
    }
}

package com.roknauta.milheiro.web.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.NumberConverter;
import jakarta.faces.convert.FacesConverter;
import java.util.Locale;

/** Preserva as casas decimais de fatores e percentuais, que não são quantidades de pontos. */
@FacesConverter("decimalConverter")
public class DecimalConverter extends NumberConverter {
    public DecimalConverter() {
        setLocale(Locale.forLanguageTag("pt-BR"));
        setGroupingUsed(true);
        setMinFractionDigits(3);
        setMaxFractionDigits(3);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Object configured = component.getAttributes().get("casasDecimais");
        int digits = configured == null ? 3 : Integer.parseInt(configured.toString());
        setMinFractionDigits(digits);
        setMaxFractionDigits(digits);
        return super.getAsString(context, component, value);
    }
}

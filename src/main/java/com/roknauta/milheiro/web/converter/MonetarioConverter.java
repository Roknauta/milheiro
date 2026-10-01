package com.roknauta.milheiro.web.converter;

import com.roknauta.milheiro.domain.Dinheiro;
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
    @Override
    public String getAsString(jakarta.faces.context.FacesContext context,
        jakarta.faces.component.UIComponent component, Object valor) {
        if (valor instanceof java.math.BigDecimal decimal) {
            return Dinheiro.de(decimal).formatar(codigoMoeda(), getLocale());
        }
        return super.getAsString(context, component, valor);
    }

    protected String codigoMoeda() { return "BRL"; }
}

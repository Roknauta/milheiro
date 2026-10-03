package com.roknauta.milheiro.web.converter;

import com.roknauta.milheiro.domain.Dinheiro;
import com.roknauta.milheiro.web.Msg;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

@FacesConverter(forClass = Dinheiro.class)
public class DinheiroConverter implements Converter<Dinheiro> {
    private DecimalFormat formato() {
        DecimalFormat formato = (DecimalFormat) NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
        formato.setParseBigDecimal(true);
        formato.setMaximumFractionDigits(340);
        return formato;
    }

    @Override
    public Dinheiro getAsObject(FacesContext context, UIComponent component, String texto) {
        if (texto == null || texto.trim().isEmpty()) return null;
        String entrada = texto.trim();
        // InputNumber submete o número normalizado, sem agrupamento e com ponto decimal.
        if (component instanceof org.primefaces.component.inputnumber.InputNumber) {
            try {
                return new Dinheiro(entrada);
            } catch (NumberFormatException erro) {
                String mensagem = Msg.get("componente.dinheiro.invalido");
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, mensagem));
            }
        }
        ParsePosition posicao = new ParsePosition(0);
        Number valor = formato().parse(entrada, posicao);
        if (valor == null || posicao.getIndex() != entrada.length()) {
            String mensagem = Msg.get("componente.dinheiro.invalido");
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, mensagem));
        }
        return Dinheiro.de((BigDecimal) valor);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Dinheiro valor) {
        if (valor == null) return "";
        if (component instanceof org.primefaces.component.inputnumber.InputNumber) return valor.toPlainString();
        return formato().format(valor);
    }
}

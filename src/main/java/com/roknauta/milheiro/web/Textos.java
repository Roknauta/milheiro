package com.roknauta.milheiro.web;

import java.util.Locale;
import java.util.ResourceBundle;

public final class Textos {

    private Textos() {
    }

    public static String formatar(String chave, Object... argumentos) {
        return new java.text.MessageFormat(get(chave), Locale.forLanguageTag("pt-BR")).format(argumentos);
    }

    public static String get(String chave) {
        return ResourceBundle.getBundle("messages", Locale.forLanguageTag("pt-BR")).getString(chave);
    }
}

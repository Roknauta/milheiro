package com.roknauta.milheiro.web;

import java.util.Locale;
import java.util.ResourceBundle;
import java.text.MessageFormat;

public final class Msg {

    private static final Locale LOCALE = Locale.forLanguageTag("pt-BR");

    private Msg() {
    }

    public static String formatar(String chave, Object... argumentos) {
        return get(chave, argumentos);
    }

    public static String get(String chave) {
        return ResourceBundle.getBundle("messages", LOCALE).getString(chave);
    }

    public static String get(String chave, Object... argumentos) {
        return new MessageFormat(get(chave), LOCALE).format(argumentos);
    }
}

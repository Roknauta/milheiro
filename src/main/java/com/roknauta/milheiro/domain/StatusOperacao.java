package com.roknauta.milheiro.domain;

import com.roknauta.milheiro.web.Textos;

public enum StatusOperacao {
    CONFIRMADO, PENDENTE, CANCELADO;

    public String getDescricao() {
        return Textos.get("operacao.status." + name().toLowerCase(java.util.Locale.ROOT));
    }
}

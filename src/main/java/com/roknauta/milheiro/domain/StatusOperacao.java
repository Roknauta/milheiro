package com.roknauta.milheiro.domain;

import com.roknauta.milheiro.web.Msg;

public enum StatusOperacao {
    CONFIRMADO, PENDENTE, CANCELADO;

    public String getDescricao() {
        return Msg.get("operacao.status." + name().toLowerCase(java.util.Locale.ROOT));
    }
}

package com.roknauta.milheiro.domain;

import com.roknauta.milheiro.web.Msg;

public enum ParcelaTransferencia {
    BASE, BONUS;
    public String getDescricao() {
        return Msg.get("operacao.parcela." + name().toLowerCase(java.util.Locale.ROOT));
    }
}

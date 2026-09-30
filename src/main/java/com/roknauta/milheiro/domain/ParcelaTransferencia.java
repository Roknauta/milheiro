package com.roknauta.milheiro.domain;

import com.roknauta.milheiro.web.Textos;

public enum ParcelaTransferencia {
    BASE, BONUS;
    public String getDescricao() {
        return Textos.get("operacao.parcela." + name().toLowerCase(java.util.Locale.ROOT));
    }
}

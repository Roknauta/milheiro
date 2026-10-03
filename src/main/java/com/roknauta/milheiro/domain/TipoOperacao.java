package com.roknauta.milheiro.domain;

import com.roknauta.milheiro.web.Msg;

public enum TipoOperacao {
    ACUMULO("mensagem.acumulo"), TRANSFERENCIA("mensagem.transferencia"), VENDA("mensagem.venda"), RESGATE(
        "mensagem.resgate"), ESTORNO("mensagem.estorno");
    private final String descricao;

    TipoOperacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return Msg.get(descricao);
    }
}

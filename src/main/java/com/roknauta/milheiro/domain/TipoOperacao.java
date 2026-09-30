package com.roknauta.milheiro.domain;
public enum TipoOperacao {
    ACUMULO("mensagem.acumulo"), TRANSFERENCIA("mensagem.transferencia"), VENDA("mensagem.venda"), RESGATE("mensagem.resgate"), ESTORNO("mensagem.estorno");
    private final String descricao;
    TipoOperacao(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return com.roknauta.milheiro.web.Textos.get(descricao); }
}

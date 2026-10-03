package com.roknauta.milheiro.dto.crud;

import com.roknauta.milheiro.domain.TipoOperacao;

public class VendaDTO extends OperacaoDTO {
    public VendaDTO() {
        setTipo(TipoOperacao.VENDA);
    }
}

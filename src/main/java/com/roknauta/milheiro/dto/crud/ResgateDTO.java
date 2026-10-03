package com.roknauta.milheiro.dto.crud;

import com.roknauta.milheiro.domain.TipoOperacao;

public class ResgateDTO extends OperacaoDTO {
    public ResgateDTO() {
        setTipo(TipoOperacao.RESGATE);
    }
}

package com.roknauta.milheiro.dto.crud;

import com.roknauta.milheiro.domain.TipoOperacao;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstornoDTO extends OperacaoDTO {
    private Long operacaoOriginalId;
    private Long versaoOriginal;
    private OperacaoDTO operacaoOriginal;

    public EstornoDTO() { setTipo(TipoOperacao.ESTORNO); }
}

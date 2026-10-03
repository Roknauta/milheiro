package com.roknauta.milheiro.dto.crud;

import com.roknauta.milheiro.domain.TipoOperacao;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcumuloDTO extends OperacaoDTO {
    private Long vinculoTransferencia;
    private String parcelaDescricao;

    public AcumuloDTO() { setTipo(TipoOperacao.ACUMULO); }
}

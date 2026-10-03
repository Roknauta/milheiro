package com.roknauta.milheiro.dto.crud;

import com.roknauta.milheiro.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class FatorConversaoDTO extends BaseDTO {
    private Long origemId;
    private Long destinoId;
    private ProgramaFidelidadeDTO origem;
    private ProgramaFidelidadeDTO destino;
    private BigDecimal pontosOrigem = BigDecimal.ONE;
    private BigDecimal pontosDestino = BigDecimal.ONE;
    private String filtro;
}

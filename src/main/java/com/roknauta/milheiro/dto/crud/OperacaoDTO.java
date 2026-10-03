package com.roknauta.milheiro.dto.crud;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Atributos compartilhados pelos formulários, consultas e lançamentos de operações. */
@Getter
@Setter
public class OperacaoDTO extends BaseDTO {
    private Long versao;
    private LocalDate data = LocalDate.now();
    private TipoOperacao tipo;
    private BigDecimal quantidade = BigDecimal.ZERO;
    private Dinheiro valor = Dinheiro.ZERO;
    private Dinheiro taxas = Dinheiro.ZERO;
    private String observacoes;
    private Long programaId;
    private ProgramaFidelidadeDTO programa;
    private StatusOperacao status;
    private String filtro;

    public boolean isConfirmada() { return status == StatusOperacao.CONFIRMADO; }
    public boolean isCancelada() { return status == StatusOperacao.CANCELADO; }
    public boolean isTransferencia() { return tipo == TipoOperacao.TRANSFERENCIA; }
}

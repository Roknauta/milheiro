package com.roknauta.milheiro.dto;

import com.roknauta.milheiro.domain.TipoOperacao;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class OperacaoFormulario implements Serializable {
    private LocalDate data = LocalDate.now();
    private TipoOperacao tipo = TipoOperacao.ACUMULO;
    private BigDecimal quantidade = BigDecimal.ZERO;
    private BigDecimal valor = BigDecimal.ZERO;
    private BigDecimal taxas = BigDecimal.ZERO;
    private BigDecimal pontosOrigem = BigDecimal.ONE;
    private BigDecimal pontosDestino = BigDecimal.ONE;
    private BigDecimal bonus = BigDecimal.ZERO;
    private boolean comCarrinho;
    private BigDecimal pontosDebitarSaldo;
    private BigDecimal valorCarrinho;
    private String observacoes;
    private String chaveImportacao;
    private Long operacaoOriginalId;
    private Long versaoOriginal;

    public boolean isTransferencia() { return tipo == TipoOperacao.TRANSFERENCIA; }
    public boolean isEstorno() { return tipo == TipoOperacao.ESTORNO; }
    public ParametrosTransferencia parametros() {
        return new ParametrosTransferencia(quantidade, pontosOrigem, pontosDestino, bonus,
            comCarrinho, pontosDebitarSaldo, valorCarrinho, taxas);
    }
}

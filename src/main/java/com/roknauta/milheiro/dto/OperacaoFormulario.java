package com.roknauta.milheiro.dto;

import com.roknauta.milheiro.domain.Dinheiro;
import com.roknauta.milheiro.domain.TipoOperacao;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class OperacaoFormulario implements Serializable {
    private Long id;
    private Long versao;
    private Dinheiro valorAdicional = Dinheiro.ZERO;
    private LocalDate data = LocalDate.now();
    private TipoOperacao tipo = TipoOperacao.ACUMULO;
    private BigDecimal quantidade = BigDecimal.ZERO;
    private Dinheiro valor = Dinheiro.ZERO;
    private Dinheiro taxas = Dinheiro.ZERO;
    private BigDecimal pontosOrigem = BigDecimal.ONE;
    private BigDecimal pontosDestino = BigDecimal.ONE;
    private BigDecimal bonus = BigDecimal.ZERO;
    private boolean comCarrinho;
    private BigDecimal pontosDebitarSaldo;
    private Dinheiro valorCarrinho;
    private String observacoes;
    private Long operacaoOriginalId;
    private Long versaoOriginal;

    public boolean isTransferencia() { return tipo == TipoOperacao.TRANSFERENCIA; }
    public boolean isEstorno() { return tipo == TipoOperacao.ESTORNO; }
    public ParametrosTransferencia parametros() {
        return new ParametrosTransferencia(quantidade, pontosOrigem, pontosDestino, bonus,
            comCarrinho, pontosDebitarSaldo, valorCarrinho, taxas);
    }
}

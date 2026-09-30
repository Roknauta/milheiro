package com.roknauta.milheiro.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record ResultadoTransferencia(BigDecimal total, BigDecimal debito, BigDecimal base,
    BigDecimal bonus, BigDecimal creditos, BigDecimal comprados, BigDecimal creditosCarrinho,
    BigDecimal milheiroCarrinho, BigDecimal custoOrigem, BigDecimal custoTotal,
    BigDecimal desembolso, BigDecimal percentual, boolean saldoInsuficiente) implements Serializable { }

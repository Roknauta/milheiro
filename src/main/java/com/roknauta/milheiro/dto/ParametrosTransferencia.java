package com.roknauta.milheiro.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record ParametrosTransferencia(BigDecimal quantidade, BigDecimal pontosOrigem,
    BigDecimal pontosDestino, BigDecimal bonus, boolean comCarrinho, BigDecimal pontosDebitarSaldo,
    BigDecimal valorCarrinho, BigDecimal taxas) implements Serializable { }

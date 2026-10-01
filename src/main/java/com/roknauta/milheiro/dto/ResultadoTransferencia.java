package com.roknauta.milheiro.dto;

import com.roknauta.milheiro.domain.Dinheiro;
import java.io.Serializable;
import java.math.BigDecimal;

public record ResultadoTransferencia(BigDecimal total, BigDecimal debito, BigDecimal base,
    BigDecimal bonus, BigDecimal creditos, BigDecimal comprados, BigDecimal creditosCarrinho,
    Dinheiro milheiroCarrinho, Dinheiro custoOrigem, Dinheiro custoTotal,
    Dinheiro valorAdicional, BigDecimal percentual, boolean saldoInsuficiente) implements Serializable { }

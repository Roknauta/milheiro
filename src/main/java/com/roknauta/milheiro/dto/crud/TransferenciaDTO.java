package com.roknauta.milheiro.dto.crud;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.dto.ParametrosTransferencia;
import com.roknauta.milheiro.dto.ResultadoTransferencia;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class TransferenciaDTO extends OperacaoDTO {
    private Long destinoId;
    private ProgramaFidelidadeDTO destino;
    private Dinheiro valorAdicional = Dinheiro.ZERO;
    private BigDecimal pontosOrigem = BigDecimal.ONE;
    private BigDecimal pontosDestino = BigDecimal.ONE;
    private BigDecimal bonus = BigDecimal.ZERO;
    private boolean comCarrinho;
    private BigDecimal pontosDebitarSaldo;
    private Dinheiro valorCarrinho;
    private boolean conversaoExcepcional;
    private FatorConversaoDTO fatorCadastrado;
    private ResultadoTransferencia resultadoTransferencia;
    private BigDecimal resumoPontos;
    private BigDecimal resumoPontosCarrinho;
    private Dinheiro resumoCusto;
    private Dinheiro resumoTotal;
    private Dinheiro resumoEuros;
    private Dinheiro resumoMilheiroCarrinho;
    private String resumoAviso;
    private Dinheiro custoPorEuro;
    private BigDecimal pontosDebitados;
    private BigDecimal pontosComprados;
    private Dinheiro custoCarrinho = Dinheiro.ZERO;

    public TransferenciaDTO() { setTipo(TipoOperacao.TRANSFERENCIA); }

    public ParametrosTransferencia parametros() {
        return new ParametrosTransferencia(getQuantidade(), pontosOrigem, pontosDestino, bonus,
                comCarrinho, pontosDebitarSaldo, valorCarrinho, getTaxas());
    }
}

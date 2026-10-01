package com.roknauta.milheiro.web;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.dto.ResultadoTransferencia;
import com.roknauta.milheiro.helper.TransferenciaHelper;
import com.roknauta.milheiro.service.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.*;

@Component("transferenciaController")
@Scope("view")
@Getter
@Setter
public class TransferenciaController extends OperacaoController<Transferencia> {
    private Long destinoOperacao;
    private boolean conversaoExcepcional;
    private List<FatorConversao> fatores = List.of();
    private ResultadoTransferencia resultadoTransferencia;

    public TransferenciaController(OperacaoService service) {
        super(service, TipoOperacao.TRANSFERENCIA, Transferencia.class);
    }


    @Override
    protected void limparEspecificos() {
        destinoOperacao = null;
        conversaoExcepcional = false;
        fatores = service.fatores();
        atualizarResumoTransferencia();
    }

    @Override
    protected boolean corresponde(Transferencia item) {
        return super.corresponde(item) || contem(item.getDestino().getNome());
    }

    private BigDecimal resumoPontos, resumoPontosCarrinho;
    private Dinheiro resumoCusto, resumoTotal, resumoEuros, resumoMilheiroCarrinho;
    private String resumoAviso;

    public void atualizarResumoTransferencia() {
        resumoPontos = resumoPontosCarrinho = null;
        resumoCusto = resumoTotal = resumoEuros = resumoMilheiroCarrinho = null;
        resumoAviso = null;
        resultadoTransferencia = null;
        if (!operacao.isTransferencia() || operacao.getData() == null || programaOperacao == null || destinoOperacao == null)
            return;
        try {
            if (Objects.equals(programaOperacao, destinoOperacao))
                throw new IllegalArgumentException(Textos.get("interface.origem.e.destino.devem.ser.diferentes"));
            Resumo origem = service.resumoAntesDaOperacao(operacao.getData(), programaOperacao);
            if (origem == null) return;
            resultadoTransferencia = TransferenciaHelper.calcular(operacao.parametros(), origem,
                false, 2, java.math.RoundingMode.DOWN);
            BigDecimal pontos = resultadoTransferencia.creditos();
            BigDecimal debito = resultadoTransferencia.debito();
            resumoPontos = pontos;
            resumoCusto = resultadoTransferencia.custoOrigem();
            resumoTotal = resultadoTransferencia.custoTotal();
            resumoPontosCarrinho = resultadoTransferencia.creditosCarrinho();
            resumoMilheiroCarrinho = resultadoTransferencia.milheiroCarrinho();
            if (programas.stream().anyMatch(p -> Objects.equals(p.getId(), destinoOperacao) && p.isAll()))
                resumoEuros = Calculos.eurosAll(pontos);
            if (debito.compareTo(origem.getSaldo()) > 0)
                resumoAviso = Textos.get("transferencia.saldo.insuficiente");
        } catch (IllegalArgumentException e) {
            resumoAviso = e.getMessage();
        }
    }

    public void aplicarFator() {
        if (!operacao.isTransferencia()) {
            atualizarResumoTransferencia();
            return;
        }
        fatores = service.fatores();
        FatorConversao f = getFatorCadastrado();
        conversaoExcepcional = f == null;
        if (f != null) {
            operacao.setPontosOrigem(f.getPontosOrigem());
            operacao.setPontosDestino(f.getPontosDestino());
            operacao.setBonus(BigDecimal.ZERO);
        } else {
            operacao.setPontosOrigem(BigDecimal.ONE);
            operacao.setPontosDestino(BigDecimal.ONE);
            operacao.setBonus(BigDecimal.ZERO);
        }
        atualizarResumoTransferencia();
    }

    public void alternarConversaoExcepcional() {
        if (!conversaoExcepcional) {
            // Restaurar a proporção não descarta o bônus que já foi informado.
            BigDecimal bonus = operacao.getBonus();
            aplicarFator();
            operacao.setBonus(bonus);
        }
        atualizarResumoTransferencia();
    }

    public FatorConversao getFatorCadastrado() {
        return fatores.stream().filter(
            f -> Objects.equals(f.getOrigem().getId(), programaOperacao) && Objects.equals(f.getDestino().getId(),
                destinoOperacao)).findFirst().orElse(null);
    }

    public Dinheiro getCustoPorEuro() {
        BigDecimal euros = resumoEuros;
        return TransferenciaHelper.custoPorEuro(resumoTotal, euros);
    }

    public BigDecimal getPontosDebitados() {
        return resultadoTransferencia == null ? null : resultadoTransferencia.debito();
    }
    public BigDecimal getPontosComprados() {
        return resultadoTransferencia == null ? null : resultadoTransferencia.comprados();
    }
    public Dinheiro getCustoCarrinho() {
        return operacao.isComCarrinho() ? operacao.getValorCarrinho() : Dinheiro.ZERO;
    }

    @Override
    protected Transferencia salvarOperacao() {
        return service.salvarTransferencia(operacao, programaOperacao, destinoOperacao);
    }
    @Override
    protected void prepararEdicao(Transferencia item) {
        destinoOperacao = item.getDestino().getId();
        operacao.setValorAdicional(item.getValorAdicional());
    }
}

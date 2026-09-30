package com.roknauta.milheiro.helper;

import com.roknauta.milheiro.dto.*;
import com.roknauta.milheiro.service.*;
import com.roknauta.milheiro.web.Textos;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class TransferenciaHelper {
    private TransferenciaHelper() { }

    public static ResultadoTransferencia calcular(ParametrosTransferencia p, Resumo origem,
        boolean receber, int escala, RoundingMode arredondamento) {
        Calculos.positivo(p.quantidade(), Textos.get("campo.quantidade"));
        Calculos.positivo(p.pontosOrigem(), Textos.get("interface.proporcao.de.origem"));
        BigDecimal multiplicador = Calculos.multiplicador(p.pontosDestino(), p.bonus());
        BigDecimal total = receber ? p.quantidade().multiply(p.pontosOrigem())
            .divide(multiplicador, 3, RoundingMode.CEILING) : p.quantidade();
        BigDecimal debito = p.comCarrinho() ? p.pontosDebitarSaldo() : total;
        BigDecimal compra = p.comCarrinho() ? p.valorCarrinho() : BigDecimal.ZERO;
        Calculos.naoNegativo(debito, Textos.get("interface.pontos.debitados.do.saldo.da.origem"));
        Calculos.naoNegativo(compra, Textos.get("interface.valor.do.carrinho"));
        Calculos.naoNegativo(p.taxas(), Textos.get("campo.taxas"));
        if (debito.compareTo(total) > 0)
            throw new IllegalArgumentException(Textos.get("interface.os.pontos.a.debitar.do.saldo.nao.podem.superar.o.total.da.transferencia"));
        BigDecimal comprados = total.subtract(debito);
        if (comprados.signum() == 0 && compra.signum() > 0)
            throw new IllegalArgumentException(Textos.get("interface.nao.ha.pontos.a.comprar.no.carrinho.informe.valor.zero.ou.reduza.os.pontos.a.d"));
        BigDecimal base = Calculos.transferirProporcao(total, p.pontosOrigem(), p.pontosDestino(),
            BigDecimal.ZERO, escala, arredondamento);
        BigDecimal creditos = total.multiply(multiplicador).divide(p.pontosOrigem(), escala, arredondamento);
        BigDecimal creditosCarrinho = comprados.multiply(multiplicador)
            .divide(p.pontosOrigem(), 12, RoundingMode.HALF_UP);
        BigDecimal custo = debito.signum() == 0 ? BigDecimal.ZERO
            : Calculos.proporcional(debito, origem.getAcumulado(), origem.getGasto());
        BigDecimal percentual = debito.signum() == 0 ? BigDecimal.ZERO
            : debito.multiply(new BigDecimal("100")).divide(origem.getAcumulado(), 12, RoundingMode.HALF_UP);
        BigDecimal milheiro = creditosCarrinho.signum() == 0 ? null
            : compra.multiply(new BigDecimal("1000")).divide(creditosCarrinho, 12, RoundingMode.HALF_UP);
        BigDecimal desembolso = compra.add(p.taxas());
        return new ResultadoTransferencia(total, debito, base, creditos.subtract(base), creditos,
            comprados, creditosCarrinho, milheiro, custo, custo.add(desembolso), desembolso,
            percentual, debito.compareTo(origem.getSaldo()) > 0);
    }

    public static BigDecimal custoPorEuro(BigDecimal custo, BigDecimal euros) {
        return custo == null || euros == null || euros.signum() <= 0 ? null
            : custo.divide(euros, 6, RoundingMode.HALF_UP);
    }
}

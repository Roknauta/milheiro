package com.roknauta.milheiro.web;

import com.roknauta.milheiro.domain.FatorConversao;
import com.roknauta.milheiro.domain.Dinheiro;
import com.roknauta.milheiro.dto.*;
import com.roknauta.milheiro.helper.TransferenciaHelper;
import com.roknauta.milheiro.domain.ProgramaFidelidade;
import com.roknauta.milheiro.service.OperacaoService;
import com.roknauta.milheiro.service.Resumo;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Component("calculadora")
@Scope("view")
@Getter
@Setter
public class CalculadoraBean implements Serializable {

    private final OperacaoService service;
    private List<ProgramaFidelidade> programas;
    private List<Resumo> resumos;
    private Long origemId, destinoId;
    private String fator;
    private BigDecimal pontos, bonus = BigDecimal.ZERO;
    private boolean comCarrinho;
    private BigDecimal pontosDebitarSaldo;
    private Dinheiro valorCarrinho;
    private BigDecimal pontosCarrinho;
    private BigDecimal totalTransferencia, pontosCarrinhoCreditar;
    private Dinheiro milheiroCarrinho;
    private BigDecimal pontosTransferir, pontosCreditar, percentual;
    private Dinheiro gastoPercentual, valorDestino;
    private String orientacao, aviso;
    private boolean calculado;
    private boolean fatorDoCadastro;

    public CalculadoraBean(OperacaoService service) {
        this.service = service;
    }

    @PostConstruct
    public void carregar() {
        programas = service.programas().stream().filter(ProgramaFidelidade::isAtivo).toList();
        resumos = service.resumos();
    }

    public Dinheiro getEurosGerados() {
        return calculado && programas.stream().anyMatch(p -> Objects.equals(p.getId(), destinoId) && p.isAll())
            ? com.roknauta.milheiro.service.Calculos.eurosAll(pontosCreditar)
            : null;
    }

    public Resumo getOrigem() {
        return resumos.stream().filter(r -> Objects.equals(r.getPrograma().getId(), origemId)).findFirst().orElse(null);
    }

    public void selecionarProgramas(boolean receber) {
        resumos = service.resumos();
        fator = null;
        bonus = BigDecimal.ZERO;
        fatorDoCadastro = false;
        for (FatorConversao f : service.fatores()) {
            if (Objects.equals(f.getOrigem().getId(), origemId) && Objects.equals(f.getDestino().getId(), destinoId)) {
                fator = formatar(f.getPontosOrigem()) + " : " + formatar(f.getPontosDestino());
                fatorDoCadastro = true;
                break;
            }
        }
        recalcular(receber);
    }

    private String formatar(BigDecimal valor) {
        return valor.setScale(3, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    private BigDecimal lerParte(String texto) {
        // Sem agrupadores: a vírgula ou o ponto indicam a parte decimal.
        if (!texto.trim().matches("[0-9]+([,.][0-9]{1,3})?")) {
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get(
                "interface.use.o.fator.como.origem.destino.com.ate.tres.casas.decimais.ex.3.500.1.000"));
        }
        BigDecimal valor = new BigDecimal(texto.trim().replace(',', '.'));
        if (valor.signum() <= 0)
            throw new IllegalArgumentException(
                com.roknauta.milheiro.web.Textos.get("interface.as.duas.partes.do.fator.devem.ser.maiores.que.zero"));
        return valor;
    }

    public void recalcular(boolean receber) {
        calculado = false;
        pontosCarrinho = totalTransferencia = pontosCarrinhoCreditar = null;
        milheiroCarrinho = null;
        pontosTransferir = pontosCreditar = percentual = null;
        gastoPercentual = valorDestino = null;
        aviso = null;
        orientacao = com.roknauta.milheiro.web.Textos.get("interface.selecione.origem.e.destino.para.comecar");
        Resumo origem = getOrigem();
        if (origem == null || destinoId == null)
            return;
        if (Objects.equals(origemId, destinoId)) {
            orientacao =
                com.roknauta.milheiro.web.Textos.get("interface.escolha.programas.diferentes.para.origem.e.destino");
            return;
        }
        if (fator == null || fator.isBlank()) {
            orientacao = com.roknauta.milheiro.web.Textos.get(
                "interface.informe.o.fator.de.conversao.para.este.par.de.programas");
            return;
        }
        try {
            String[] partes = fator.split(":", -1);
            if (partes.length > 2)
                throw new IllegalArgumentException(
                    com.roknauta.milheiro.web.Textos.get("interface.informe.o.fator.como.origem.destino"));
            BigDecimal proporcaoOrigem = lerParte(partes[0]);
            BigDecimal proporcaoDestino = partes.length == 1 ? BigDecimal.ONE : lerParte(partes[1]);
            if (pontos == null || pontos.signum() <= 0) {
                orientacao = receber
                    ? com.roknauta.milheiro.web.Textos.get("interface.informe.os.pontos.que.deseja.receber")
                    : com.roknauta.milheiro.web.Textos.get("interface.informe.os.pontos.a.transferir");
                return;
            }
            if (bonus == null || bonus.signum() < 0) {
                orientacao =
                    com.roknauta.milheiro.web.Textos.get("interface.informe.um.bonus.valido.zero.quando.nao.houver");
                return;
            }
            ResultadoTransferencia resultado = TransferenciaHelper.calcular(
                new ParametrosTransferencia(pontos, proporcaoOrigem, proporcaoDestino, bonus,
                    comCarrinho, pontosDebitarSaldo, valorCarrinho, BigDecimal.ZERO),
                origem, receber, 12, RoundingMode.HALF_UP);
            totalTransferencia = resultado.total();
            pontosTransferir = resultado.debito();
            pontosCarrinho = resultado.comprados();
            pontosCarrinhoCreditar = resultado.creditosCarrinho();
            milheiroCarrinho = resultado.milheiroCarrinho();
            pontosCreditar = resultado.creditos();
            percentual = resultado.percentual();
            gastoPercentual = resultado.custoOrigem();
            valorDestino = resultado.custoTotal();
            if (pontosTransferir.compareTo(origem.getSaldo()) > 0)
                aviso = com.roknauta.milheiro.web.Textos.get(
                    "interface.os.pontos.necessarios.superam.o.saldo.disponivel.na.origem.este.resultado.e.ap");
            calculado = true;
            orientacao = null;
        } catch (IllegalArgumentException e) {
            orientacao = e.getMessage();
        }
    }

    public Dinheiro getCustoPorEuro() {
        BigDecimal euros = getEurosGerados();
        return TransferenciaHelper.custoPorEuro(valorDestino, euros);
    }
}

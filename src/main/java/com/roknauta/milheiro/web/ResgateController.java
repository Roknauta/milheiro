package com.roknauta.milheiro.web;

import com.roknauta.milheiro.domain.Resgate;
import com.roknauta.milheiro.domain.TipoOperacao;
import com.roknauta.milheiro.service.OperacaoService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("resgateController")
@Scope("view")
public class ResgateController extends OperacaoController<Resgate> {
    public ResgateController(OperacaoService service) {
        super(service, TipoOperacao.RESGATE, Resgate.class);
    }
    @Override
    protected Resgate salvarOperacao() {
        return service.salvarResgate(operacao, programaOperacao);
    }
    @Override
    protected void prepararEdicao(Resgate item) {
        operacao.setTaxas(item.getValor());
    }
}

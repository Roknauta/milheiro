package com.roknauta.milheiro.web;

import com.roknauta.milheiro.domain.Venda;
import com.roknauta.milheiro.domain.TipoOperacao;
import com.roknauta.milheiro.service.CarteiraService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("vendaController")
@Scope("view")
public class VendaController extends OperacaoController<Venda> {
    public VendaController(CarteiraService service) {
        super(service, TipoOperacao.VENDA, Venda.class);
    }
}

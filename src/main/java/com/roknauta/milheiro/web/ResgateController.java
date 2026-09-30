package com.roknauta.milheiro.web;

import com.roknauta.milheiro.domain.Resgate;
import com.roknauta.milheiro.domain.TipoOperacao;
import com.roknauta.milheiro.service.CarteiraService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("resgateController")
@Scope("view")
public class ResgateController extends OperacaoController<Resgate> {
    public ResgateController(CarteiraService service) {
        super(service, TipoOperacao.RESGATE, Resgate.class);
    }
}

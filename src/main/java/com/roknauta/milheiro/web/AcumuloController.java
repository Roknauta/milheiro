package com.roknauta.milheiro.web;

import com.roknauta.milheiro.domain.Acumulo;
import com.roknauta.milheiro.domain.TipoOperacao;
import com.roknauta.milheiro.service.OperacaoService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("acumuloController")
@Scope("view")
public class AcumuloController extends OperacaoController<Acumulo> {
    public AcumuloController(OperacaoService service) {
        super(service, TipoOperacao.ACUMULO, Acumulo.class);
    }

    @Override
    protected boolean corresponde(Acumulo item) {
        return super.corresponde(item) || contem(item.getParcelaDescricao())
            || (item.getVinculoTransferencia() != null && contem(item.getVinculoTransferencia().toString()));
    }
    @Override
    protected Acumulo salvarOperacao() {
        return service.salvarAcumulo(operacao, programaOperacao);
    }
    public void confirmar(Acumulo item) {
        executar(() -> service.confirmarAcumulo(item.getId(), item.getVersao()));
    }
}

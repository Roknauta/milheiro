package com.roknauta.milheiro.web;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.service.CarteiraService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.*;

@Component("estornoController")
@Scope("view")
public class EstornoController extends OperacaoController<Estorno> {
    public EstornoController(CarteiraService service) {
        super(service, TipoOperacao.ESTORNO, Estorno.class);
    }

    @Override
    protected boolean corresponde(Estorno item) {
        return super.corresponde(item) || contem(item.getOperacaoOriginal().getId().toString())
            || contem(item.getOperacaoOriginal().getTipo().getDescricao());
    }

    public List<Operacao> getOperacoesEstornaveis() {
        Set<Long> estornadas = new HashSet<>();
        historico.stream().filter(o -> o instanceof Estorno && o.isConfirmada())
            .forEach(o -> estornadas.add(((Estorno) o).getOperacaoOriginal().getId()));
        return historico.stream().filter(o -> o.isConfirmada() && !(o instanceof Estorno)
            && !estornadas.contains(o.getId())).toList();
    }

    public void selecionarOriginal() {
        Operacao original = getOperacaoOriginal();
        operacao.setVersaoOriginal(original == null ? null : original.getVersao());
    }

    public Operacao getOperacaoOriginal() {
        return historico.stream().filter(o -> Objects.equals(o.getId(), operacao.getOperacaoOriginalId()))
            .findFirst().orElse(null);
    }
}

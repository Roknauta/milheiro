package com.roknauta.milheiro.web;

import com.roknauta.milheiro.dto.crud.*;
import com.roknauta.milheiro.service.crud.EstornoService;
import com.roknauta.milheiro.web.crud.CrudControllerBase;
import jakarta.faces.view.ViewScoped;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ViewScoped
public class EstornoController extends CrudControllerBase<EstornoDTO, EstornoService> {
    public EstornoController(EstornoService service) { super(service); }

    @Override
    public String pageTitle() { return Msg.get("operacao.estorno"); }

    public List<OperacaoDTO> getOperacoesEstornaveis() { return service.operacoesEstornaveis(getEditForm()); }
    public void selecionarOriginal() { service.selecionarOriginal(getEditForm()); }

    public void cancelar(EstornoDTO dto) {
        service.cancelar(dto);
        atualizarResultados();
        addInfoMessage("interface.dados.salvos");
    }
}

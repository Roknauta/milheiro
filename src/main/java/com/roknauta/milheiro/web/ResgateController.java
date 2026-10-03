package com.roknauta.milheiro.web;

import com.roknauta.milheiro.dto.crud.ResgateDTO;
import com.roknauta.milheiro.service.crud.ResgateService;
import com.roknauta.milheiro.web.crud.CrudControllerBase;
import jakarta.faces.view.ViewScoped;
import org.springframework.stereotype.Component;

@Component
@ViewScoped
public class ResgateController extends CrudControllerBase<ResgateDTO, ResgateService> {
    public ResgateController(ResgateService service) { super(service); }

    @Override
    public String pageTitle() { return Msg.get("operacao.resgate"); }

    public void cancelar(ResgateDTO dto) {
        service.cancelar(dto);
        atualizarResultados();
        addInfoMessage("interface.dados.salvos");
    }
}

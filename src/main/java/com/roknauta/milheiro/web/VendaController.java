package com.roknauta.milheiro.web;

import com.roknauta.milheiro.dto.crud.VendaDTO;
import com.roknauta.milheiro.service.crud.VendaService;
import com.roknauta.milheiro.web.crud.CrudControllerBase;
import jakarta.faces.view.ViewScoped;
import org.springframework.stereotype.Component;

@Component
@ViewScoped
public class VendaController extends CrudControllerBase<VendaDTO, VendaService> {
    public VendaController(VendaService service) { super(service); }

    @Override
    public String pageTitle() { return Msg.get("operacao.venda"); }

    public void cancelar(VendaDTO dto) {
        service.cancelar(dto);
        atualizarResultados();
        addInfoMessage("interface.dados.salvos");
    }
}

package com.roknauta.milheiro.web;

import com.roknauta.milheiro.dto.crud.AcumuloDTO;
import com.roknauta.milheiro.service.crud.AcumuloService;
import com.roknauta.milheiro.web.crud.CrudControllerBase;
import jakarta.faces.view.ViewScoped;
import org.springframework.stereotype.Component;

@Component
@ViewScoped
public class AcumuloController extends CrudControllerBase<AcumuloDTO, AcumuloService> {
    public AcumuloController(AcumuloService service) { super(service); }

    @Override
    public String pageTitle() { return Msg.get("operacao.acumulo"); }

    public void confirmar(AcumuloDTO dto) {
        service.confirmar(dto);
        atualizarResultados();
        addInfoMessage("interface.dados.salvos");
    }

    public void cancelar(AcumuloDTO dto) {
        service.cancelar(dto);
        atualizarResultados();
        addInfoMessage("interface.dados.salvos");
    }
}

package com.roknauta.milheiro.web;

import com.roknauta.milheiro.dto.crud.TransferenciaDTO;
import com.roknauta.milheiro.service.crud.TransferenciaService;
import com.roknauta.milheiro.web.crud.CrudControllerBase;
import jakarta.faces.view.ViewScoped;
import org.springframework.stereotype.Component;

@Component
@ViewScoped
public class TransferenciaController extends CrudControllerBase<TransferenciaDTO, TransferenciaService> {
    public TransferenciaController(TransferenciaService service) { super(service); }

    @Override
    public String pageTitle() { return Msg.get("operacao.transferencia"); }

    public void atualizarResumoTransferencia() { service.atualizarResumo(getEditForm()); }
    public void aplicarFator() { service.aplicarFator(getEditForm()); }
    public void alternarConversaoExcepcional() { service.alternarConversaoExcepcional(getEditForm()); }

    public void cancelar(TransferenciaDTO dto) {
        service.cancelar(dto);
        atualizarResultados();
        addInfoMessage("interface.dados.salvos");
    }
}

package com.roknauta.milheiro.web.crud;

import com.roknauta.milheiro.dto.crud.ProgramaFidelidadeDTO;
import com.roknauta.milheiro.service.crud.ProgramaFidelidadeService;
import com.roknauta.milheiro.web.Msg;
import jakarta.faces.view.ViewScoped;
import org.springframework.stereotype.Component;

@Component
@ViewScoped
public class ProgramaFidelidadeController extends CrudControllerBase<ProgramaFidelidadeDTO,ProgramaFidelidadeService> {

    @Override
    public String pageTitle() {
        return Msg.get("programa.fidelidade");
    }

    public ProgramaFidelidadeController(ProgramaFidelidadeService programaFidelidadeService) {
        super(programaFidelidadeService);
    }
}

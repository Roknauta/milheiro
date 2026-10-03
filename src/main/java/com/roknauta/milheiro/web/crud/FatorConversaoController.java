package com.roknauta.milheiro.web.crud;

import com.roknauta.milheiro.dto.crud.FatorConversaoDTO;
import com.roknauta.milheiro.service.crud.FatorConversaoService;
import com.roknauta.milheiro.web.Msg;
import jakarta.faces.view.ViewScoped;
import org.springframework.stereotype.Component;

@Component
@ViewScoped
public class FatorConversaoController extends CrudControllerBase<FatorConversaoDTO, FatorConversaoService> {
    public FatorConversaoController(FatorConversaoService service) { super(service); }

    @Override
    public String pageTitle() { return Msg.get("fator.conversao"); }
}

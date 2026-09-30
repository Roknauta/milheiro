package com.roknauta.milheiro.web;

import jakarta.faces.context.FacesContext;
import org.springframework.stereotype.Component;

@Component("layoutController")
public class LayoutController {

    public void prepararSessao() {
        // O escopo de view precisa da sessão antes de qualquer parte do HTML ser enviada.
        FacesContext.getCurrentInstance().getExternalContext().getSession(true);
    }
}

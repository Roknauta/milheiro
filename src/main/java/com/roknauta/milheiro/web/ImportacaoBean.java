package com.roknauta.milheiro.web;

import com.roknauta.milheiro.service.PlanilhaService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.Part;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;

import java.io.InputStream;
import java.util.Locale;

@Component("importacao")
@Scope("request")
@Getter
@Setter
public class ImportacaoBean {

    private final PlanilhaService service;
    private Part arquivo;
    private String resultado;

    public ImportacaoBean(PlanilhaService service) {
        this.service = service;
    }

    public void importar() {
        try {
            if (arquivo == null || arquivo.getSize() == 0 || arquivo.getSize() > PlanilhaService.LIMITE_BYTES || !arquivo.getSubmittedFileName()
                .toLowerCase(Locale.ROOT).endsWith(".xlsx"))
                throw new IllegalArgumentException(
                    Msg.get("interface.selecione.um.arquivo.xlsx.de.ate.5.mb"));
            try (InputStream in = arquivo.getInputStream()) {
                var r = service.importar(in.readNBytes(PlanilhaService.LIMITE_BYTES + 1));
                resultado = r.importadas() + " " + Msg.get(
                    "interface.operacoes.importadas") + " " + r.programasCriados() + " " + Msg.get(
                    "interface.programas.criados.e") + " " + r.transferencias() + " " + Msg.get(
                    "interface.transferencias.consolidadas");
            }
        } catch (IllegalArgumentException e) {
            mensagem(e.getMessage());
        } catch (Exception e) {
            mensagem(Msg.get(
                "interface.a.importacao.nao.foi.concluida.nenhum.registro.deste.arquivo.foi.gravado.verif"));
        } finally {
            if (arquivo != null) {
                try {
                    arquivo.delete();
                } catch (Exception ignored) {
                }
                arquivo = null;
            }
        }
    }

    private void mensagem(String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, texto, null));
    }
}

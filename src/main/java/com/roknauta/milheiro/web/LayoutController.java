package com.roknauta.milheiro.web;

import jakarta.faces.context.FacesContext;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("layoutController")
public class LayoutController {

    private record Caminho(String grupo, String pagina) { }

    private static final Map<String, Caminho> CAMINHOS = Map.ofEntries(
        Map.entry("/dashboard.xhtml", new Caminho(null, "interface.dashboard")),
        Map.entry("/importar.xhtml", new Caminho("interface.milheiro", "interface.importar.de.planilha")),
        Map.entry("/acumulo.xhtml", new Caminho("operacao.menu", "operacao.acumulo")),
        Map.entry("/transferencia.xhtml", new Caminho("operacao.menu", "operacao.transferencia")),
        Map.entry("/venda.xhtml", new Caminho("operacao.menu", "operacao.venda")),
        Map.entry("/resgate.xhtml", new Caminho("operacao.menu", "operacao.resgate")),
        Map.entry("/estorno.xhtml", new Caminho("operacao.menu", "operacao.estorno")),
        Map.entry("/enviar.xhtml", new Caminho("interface.calculadora", "interface.enviar.2")),
        Map.entry("/receber.xhtml", new Caminho("interface.calculadora", "interface.receber.2")),
        Map.entry("/programa-fidelidade.xhtml", new Caminho("interface.cadastros", "programa.fidelidade")),
        Map.entry("/fator-conversao.xhtml", new Caminho("interface.cadastros", "fator.conversao")),
        Map.entry("/operacoes.xhtml", new Caminho("operacao.menu", "operacao.acumulo")),
        Map.entry("/incluir-operacao.xhtml", new Caminho("operacao.menu", "operacao.acumulo"))
    );

    private Caminho caminhoAtual() {
        FacesContext contexto = FacesContext.getCurrentInstance();
        return contexto == null || contexto.getViewRoot() == null
            ? null : CAMINHOS.get(contexto.getViewRoot().getViewId());
    }

    public String getGrupoAtual() {
        Caminho caminho = caminhoAtual();
        return caminho == null || caminho.grupo() == null ? null : Msg.get(caminho.grupo());
    }

    public String getPaginaAtual() {
        Caminho caminho = caminhoAtual();
        return caminho == null ? null : Msg.get(caminho.pagina());
    }

    public void prepararSessao() {
        // O escopo de view precisa da sessão antes de qualquer parte do HTML ser enviada.
        FacesContext.getCurrentInstance().getExternalContext().getSession(true);
    }
}

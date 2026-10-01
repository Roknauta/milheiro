package com.roknauta.milheiro.web;

import com.roknauta.milheiro.domain.Dinheiro;
import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.service.*;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@Component("carteira")
@Scope("view")
@Getter
@Setter
public class CarteiraBean implements Serializable {

    private final OperacaoService service;
    private boolean pesquisaRealizada;
    private boolean editando;

    public void voltarPesquisa() {
        editando = false;
    }

    private String filtroPesquisa = "";
    private String filtroAplicado = "";

    public void pesquisar() {
        carregar();
        filtroAplicado = filtroPesquisa == null ? "" : filtroPesquisa.trim().toLowerCase(Locale.ROOT);
        pesquisaRealizada = true;
    }

    private boolean corresponde(String texto) {
        return texto != null && texto.toLowerCase(Locale.ROOT).contains(filtroAplicado);
    }

    public List<FatorConversao> getFatoresEncontrados() {
        return pesquisaRealizada
            ? fatores.stream()
            .filter(f -> corresponde(f.getOrigem().getNome()) || corresponde(f.getDestino().getNome())).toList()
            : List.of();
    }

    private FatorConversao fator = new FatorConversao();
    private Long origemFator, destinoFator;
    private List<ProgramaFidelidade> programas;
    private List<FatorConversao> fatores;

    public CarteiraBean(OperacaoService service) {
        this.service = service;
    }

    @PostConstruct
    public void carregar() {
        programas = service.programas();
        fatores = service.fatores();
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
            carregar();
            mensagem(FacesMessage.SEVERITY_INFO, com.roknauta.milheiro.web.Textos.get("interface.dados.salvos"));
        } catch (IllegalArgumentException e) {
            mensagem(FacesMessage.SEVERITY_ERROR, e.getMessage());
        } catch (org.springframework.dao.OptimisticLockingFailureException e) {
            mensagem(FacesMessage.SEVERITY_ERROR,
                com.roknauta.milheiro.web.Textos.get("interface.o.registro.mudou.em.outra.janela.recarregue.a.pagina"));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            mensagem(FacesMessage.SEVERITY_ERROR, com.roknauta.milheiro.web.Textos.get(
                "interface.nao.foi.possivel.salvar.confira.duplicidades.e.vinculos.do.registro"));
        }
    }

    private void mensagem(FacesMessage.Severity nivel, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(nivel, texto, null));
    }

    public void salvarFator() {
        executar(() -> {
            service.salvarFator(fator, origemFator, destinoFator);
            novoFator();
            editando = false;
        });
    }

    public void novoFator() {
        editando = true;
        fator = new FatorConversao();
        origemFator = null;
        destinoFator = null;
    }

    public void editarFator(FatorConversao f) {
        editando = true;
        fator = new FatorConversao();
        org.springframework.beans.BeanUtils.copyProperties(f, fator);
        origemFator = f.getOrigem().getId();
        destinoFator = f.getDestino().getId();
    }

    public void excluirFator(FatorConversao f) {
        executar(() -> {
            service.excluirFator(f.getId());
            novoFator();
            editando = false;
        });
    }

    public List<ProgramaFidelidade> getProgramasAtivos() {
        return programas.stream().filter(ProgramaFidelidade::isAtivo).toList();
    }

}

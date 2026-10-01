package com.roknauta.milheiro.web;

import com.roknauta.milheiro.domain.ProgramaFidelidade;
import com.roknauta.milheiro.service.OperacaoService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

@Component("programaFidelidadeController")
@Scope("view")
@Getter
public class ProgramaFidelidadeController implements Serializable {
    private final OperacaoService service;
    @Setter
    private ProgramaFidelidade programaFidelidade = ProgramaFidelidade.builder().build();
    private List<ProgramaFidelidade> programasFidelidade = List.of();
    private boolean pesquisaRealizada;
    private boolean editando;
    @Setter
    private String filtroPesquisa = "";
    private String filtroAplicado = "";

    public ProgramaFidelidadeController(OperacaoService service) {
        this.service = service;
    }

    @PostConstruct
    public void carregar() {
        programasFidelidade = service.programas();
    }

    public void pesquisar() {
        carregar();
        filtroAplicado = filtroPesquisa == null ? "" : filtroPesquisa.trim().toLowerCase(Locale.ROOT);
        pesquisaRealizada = true;
    }

    public List<ProgramaFidelidade> getProgramasEncontrados() {
        return pesquisaRealizada ? programasFidelidade.stream()
            .filter(p -> corresponde(p.getNome()) || corresponde(p.getCategoria() == null ? null : p.getCategoria().name()))
            .toList() : List.of();
    }

    private boolean corresponde(String texto) {
        return texto != null && texto.toLowerCase(Locale.ROOT).contains(filtroAplicado);
    }

    public void novo() {
        programaFidelidade = ProgramaFidelidade.builder().build();
        editando = true;
    }

    public void editar(ProgramaFidelidade item) {
        programaFidelidade = ProgramaFidelidade.builder().build();
        org.springframework.beans.BeanUtils.copyProperties(item, programaFidelidade);
        editando = true;
    }

    public void salvar() {
        executar(() -> {
            service.salvarProgramaFidelidade(programaFidelidade);
            programaFidelidade = ProgramaFidelidade.builder().build();
            editando = false;
        });
    }

    public void excluir(ProgramaFidelidade item) {
        executar(() -> {
            service.excluirProgramaFidelidade(item.getId());
            programaFidelidade = ProgramaFidelidade.builder().build();
            editando = false;
        });
    }

    public void voltarPesquisa() {
        editando = false;
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
            carregar();
            mensagem(FacesMessage.SEVERITY_INFO, Textos.get("interface.dados.salvos"));
        } catch (IllegalArgumentException e) {
            mensagem(FacesMessage.SEVERITY_ERROR, e.getMessage());
        } catch (org.springframework.dao.OptimisticLockingFailureException e) {
            mensagem(FacesMessage.SEVERITY_ERROR,
                Textos.get("interface.o.registro.mudou.em.outra.janela.recarregue.a.pagina"));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            mensagem(FacesMessage.SEVERITY_ERROR,
                Textos.get("interface.nao.foi.possivel.salvar.confira.duplicidades.e.vinculos.do.registro"));
        }
    }

    private void mensagem(FacesMessage.Severity nivel, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(nivel, texto, null));
    }
}

package com.roknauta.milheiro.web;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.dto.OperacaoFormulario;
import com.roknauta.milheiro.service.CarteiraService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.*;

/** Fluxo comum de consulta, inclusão e mudança de status dos lançamentos. */
@Getter
public abstract class OperacaoController<T extends Operacao> implements Serializable {
    protected final CarteiraService service;
    private final TipoOperacao tipo;
    private final Class<T> entidade;
    protected OperacaoFormulario operacao;
    protected List<Programa> programas = List.of();
    protected List<Operacao> historico = List.of();
    private List<T> registros = List.of();
    private boolean pesquisaRealizada;
    private boolean editando;
    @Setter
    private String filtroPesquisa = "";
    private String filtroAplicado = "";
    @Setter
    protected Long programaOperacao;

    protected OperacaoController(CarteiraService service, TipoOperacao tipo, Class<T> entidade) {
        this.service = service;
        this.tipo = tipo;
        this.entidade = entidade;
    }

    @PostConstruct
    public void inicializar() {
        carregar();
        novo();
        editando = false;
    }

    protected void carregar() {
        programas = service.programas();
        historico = service.operacoes();
        registros = historico.stream().filter(entidade::isInstance).map(entidade::cast).toList();
    }

    public void pesquisar() {
        carregar();
        filtroAplicado = filtroPesquisa == null ? "" : filtroPesquisa.trim().toLowerCase(Locale.ROOT);
        pesquisaRealizada = true;
    }

    public List<T> getRegistrosEncontrados() {
        return pesquisaRealizada ? registros.stream().filter(this::corresponde).toList() : List.of();
    }

    protected boolean corresponde(T item) {
        return contem(item.getPrograma().getNome()) || contem(item.getStatus().getDescricao())
            || contem(item.getObservacoes()) || contem(String.valueOf(item.getId()));
    }

    protected boolean contem(String texto) {
        return texto != null && texto.toLowerCase(Locale.ROOT).contains(filtroAplicado);
    }

    public List<Programa> getProgramasAtivos() {
        return programas.stream().filter(Programa::isAtivo).toList();
    }

    public void novo() {
        operacao = new OperacaoFormulario();
        operacao.setTipo(tipo);
        programaOperacao = null;
        limparEspecificos();
        editando = true;
    }

    protected void limparEspecificos() { }
    protected Long destinoParaSalvar() { return null; }

    public void salvar() {
        executar(() -> {
            operacao.setTipo(tipo);
            service.salvarOperacao(operacao, programaOperacao, destinoParaSalvar());
            novo();
            editando = false;
        });
    }

    public void voltarPesquisa() { editando = false; }

    public void alterarStatus(T item, String novoStatus) {
        executar(() -> service.alterarStatus(item.getId(), item.getVersao(), StatusOperacao.valueOf(novoStatus)));
    }

    protected void executar(Runnable acao) {
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

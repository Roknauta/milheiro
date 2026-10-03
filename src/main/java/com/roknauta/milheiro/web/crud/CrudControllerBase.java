package com.roknauta.milheiro.web.crud;

import com.roknauta.milheiro.dto.BaseDTO;
import com.roknauta.milheiro.service.crud.CrudService;
import com.roknauta.milheiro.web.ControllerBase;
import com.roknauta.milheiro.web.Msg;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.GenericTypeResolver;

import java.io.Serializable;
import java.util.List;

public abstract class CrudControllerBase<T extends BaseDTO, S extends CrudService<T>> extends ControllerBase
        implements Serializable {

    @Getter
    @Setter
    private T editForm;
    @Getter
    @Setter
    private T searchForm;
    @Getter
    @Setter
    private List<T> result = List.of();
    @Getter
    protected boolean editing;
    @Getter
    private boolean pesquisaRealizada;

    protected final S service;
    private Class<T> dtoClass;

    public abstract String pageTitle();

    @SuppressWarnings("unchecked")
    protected CrudControllerBase(S service) {
        this.service = service;
        setDtoClass();
    }

    @SuppressWarnings("unchecked")
    private void setDtoClass() {
        Class<?>[] typeArguments = GenericTypeResolver.resolveTypeArguments(
                getClass(), CrudControllerBase.class);

        if (typeArguments == null) {
            throw new IllegalStateException(
                    Msg.get("crud.tipo.formulario.nao.resolvido"));
        }
        dtoClass = (Class<T>) typeArguments[0];
    }

    protected T newForm() {
        try {
            return dtoClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    Msg.get("crud.formulario.erro.instanciar"), e);
        }
    }

    @PostConstruct
    public void inicializar() {
        searchForm = newForm();
    }

    public void novo() {
        editForm = newForm();
        editing = true;
    }

    public void voltarPesquisa() {
        editForm = null;
        editing = false;
    }

    public void edit(Long id) {
        editForm = service.findById(id);
        editing = true;
    }

    public void save() {
        editForm = service.save(editForm);
        aposGravar();
        atualizarResultados();
        voltarPesquisa();
        addInfoMessage("entidade.salva", pageTitle());
    }

    public void delete(Long id) {
        service.delete(id);
        finalizarExclusao(id);
    }

    public void delete(T dto) {
        service.delete(dto);
        finalizarExclusao(dto.getId());
    }

    private void finalizarExclusao(Long id) {
        if (editForm != null && id.equals(editForm.getId())) {
            voltarPesquisa();
        }
        atualizarResultados();
        addInfoMessage("crud.registro.excluido");
    }

    public void pesquisar() {
        atualizarResultados();
    }

    protected void atualizarResultados() {
        result = service.find(searchForm);
        pesquisaRealizada = true;
    }

    protected void aposGravar() { }
}

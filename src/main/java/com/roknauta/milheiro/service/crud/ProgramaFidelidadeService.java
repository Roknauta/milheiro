package com.roknauta.milheiro.service.crud;

import com.roknauta.milheiro.domain.ProgramaFidelidade;
import com.roknauta.milheiro.dto.crud.ProgramaFidelidadeDTO;
import com.roknauta.milheiro.repository.ProgramaFidelidadeRepository;
import com.roknauta.milheiro.service.mapper.ProgramaFidelidadeMapper;
import com.roknauta.milheiro.service.OperacaoService;
import com.roknauta.milheiro.web.Msg;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProgramaFidelidadeService extends CrudServiceBase<ProgramaFidelidade, ProgramaFidelidadeDTO> {

    private final ProgramaFidelidadeRepository repository;
    private final ProgramaFidelidadeMapper mapper;
    private final com.roknauta.milheiro.repository.OperacaoRepository operacoes;
    private final com.roknauta.milheiro.repository.FatorRepository fatores;
    private final com.roknauta.milheiro.repository.ConsolidadoRepository consolidados;
    private final OperacaoService operacaoService;

    @Override
    @Transactional(readOnly = true)
    public List<ProgramaFidelidadeDTO> find(ProgramaFidelidadeDTO filtro) {
        String nome = filtro.getNome() == null ? "" : filtro.getNome().trim().toLowerCase(Locale.ROOT);
        return repository.findAllByOrderByNomeAsc().stream()
                .filter(programa -> programa.getNome().toLowerCase(Locale.ROOT).contains(nome))
                .filter(programa -> filtro.getCategoria() == null || programa.getCategoria() == filtro.getCategoria())
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public ProgramaFidelidadeDTO save(ProgramaFidelidadeDTO dto) {
        ProgramaFidelidade entity;
        if (dto.getId() == null) {
            entity = mapper.toEntity(dto);
        } else {
            entity = repository.findById(dto.getId()).orElseThrow(() ->
                    new IllegalArgumentException(Msg.get("crud.registro.nao.encontrado")));
            mapper.updateEntity(dto, entity);
        }
        return mapper.toDTO(salvar(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        excluirProgramaFidelidade(id);
    }

    @Transactional
    public ProgramaFidelidade salvar(ProgramaFidelidade p) {
        repository.bloquearTodos();
        if (p.getNome() == null || p.getNome().trim().isEmpty() || p.getNome().trim().length() > 80)
            throw new IllegalArgumentException(Msg.get("interface.informe.um.nome.de.ate.80.caracteres"));
        p.setNome(p.getNome().trim());
        if (repository.existsByNomeIgnoreCaseAndIdNot(p.getNome(), p.getId() == null ? -1L : p.getId()))
            throw new IllegalArgumentException(Msg.get("interface.ja.existe.um.programa.com.esse.nome"));
        if (p.getCategoria() == null)
            throw new IllegalArgumentException(Msg.get("interface.categoria.invalida"));
        ProgramaFidelidade salvo = repository.saveAndFlush(p);
        operacaoService.atualizarConsolidados();
        return salvo;
    }

    private void excluirProgramaFidelidade(Long id) {
        repository.bloquearTodos();
        if (operacoes.findAll().stream().anyMatch(o -> Objects.equals(o.getPrograma().getId(), id)
                || (o.getDestino() != null && Objects.equals(o.getDestino().getId(), id))) || fatores.existsByOrigemIdOrDestinoId(id, id))
            throw new IllegalArgumentException(Msg.get("interface.programa.em.uso.desative.o.ou.remova.os.fatores.antes.de.excluir"));
        consolidados.deleteById(id);
        consolidados.flush();
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProgramaFidelidade> listar() {
        return repository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<ProgramaFidelidadeDTO> buscarPorNome(String nome) {
        String consulta = java.util.Optional.ofNullable(nome).orElse("").trim();
        return repository.findByAtivoTrueAndNomeContainingIgnoreCaseOrderByNomeAsc(consulta)
                .stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ProgramaFidelidade> ativos() {
        return listar().stream().filter(ProgramaFidelidade::isAtivo).toList();
    }

    @Transactional(readOnly = true)
    public ProgramaFidelidade buscar(Long id) {
        if (id == null) throw new IllegalArgumentException(Msg.get("interface.selecione.o.programa.2"));
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException(Msg.get("interface.programa.nao.encontrado")));
    }

    @Override
    protected JpaRepository<ProgramaFidelidade, Long> getRepository() {
        return repository;
    }

    @Override
    protected ProgramaFidelidadeDTO toDTO(ProgramaFidelidade entity) {
        return mapper.toDTO(entity);
    }

    @Override
    protected ProgramaFidelidade toEntity(ProgramaFidelidadeDTO dto) {
        return mapper.toEntity(dto);
    }

    @Override
    protected void updateEntity(ProgramaFidelidadeDTO dto, ProgramaFidelidade entity) {
        mapper.updateEntity(dto, entity);
    }
}

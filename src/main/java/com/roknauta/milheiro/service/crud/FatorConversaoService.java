package com.roknauta.milheiro.service.crud;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.dto.crud.FatorConversaoDTO;
import com.roknauta.milheiro.repository.FatorRepository;
import com.roknauta.milheiro.service.Calculos;
import com.roknauta.milheiro.service.mapper.FatorConversaoMapper;
import com.roknauta.milheiro.web.Msg;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FatorConversaoService implements CrudService<FatorConversaoDTO> {
    private final FatorRepository repository;
    private final ProgramaFidelidadeService programas;
    private final FatorConversaoMapper mapper;

    private FatorConversaoDTO toDTO(FatorConversao entity) {
        return mapper.toDTO(entity);
    }

    private FatorConversao buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new IllegalArgumentException(Msg.get("crud.registro.nao.encontrado")));
    }

    @Override
    @Transactional(readOnly = true)
    public FatorConversaoDTO findById(Long id) { return toDTO(buscar(id)); }

    @Override
    @Transactional(readOnly = true)
    public List<FatorConversaoDTO> findAll() { return repository.findAll().stream().map(this::toDTO).toList(); }

    @Override
    @Transactional(readOnly = true)
    public List<FatorConversaoDTO> find(FatorConversaoDTO filtro) {
        String texto = filtro.getFiltro() == null ? "" : filtro.getFiltro().trim().toLowerCase(Locale.ROOT);
        return findAll().stream().filter(dto -> dto.getOrigem().getNome().toLowerCase(Locale.ROOT).contains(texto)
                || dto.getDestino().getNome().toLowerCase(Locale.ROOT).contains(texto)).toList();
    }

    @Override
    @Transactional
    public FatorConversaoDTO save(FatorConversaoDTO dto) {
        FatorConversao entity;
        if (dto.getId() == null) {
            entity = mapper.toEntity(dto);
        } else {
            entity = buscar(dto.getId());
            mapper.updateEntity(dto, entity);
        }
        return toDTO(salvarFator(entity, dto.getOrigemId(), dto.getDestinoId()));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        buscar(id);
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<FatorConversao> listar() { return repository.findAll(); }

    private FatorConversao salvarFator(FatorConversao f, Long origem, Long destino) {
        if (java.util.Objects.equals(origem, destino))
            throw new IllegalArgumentException(Msg.get("interface.origem.e.destino.devem.ser.diferentes"));
        f.setOrigem(programas.buscar(origem));
        f.setDestino(programas.buscar(destino));
        Calculos.positivo(f.getPontosOrigem(), Msg.get("interface.pontos.de.origem"));
        Calculos.positivo(f.getPontosDestino(), Msg.get("interface.pontos.de.destino"));
        repository.findByOrigemIdAndDestinoId(origem, destino).ifPresent(existente -> {
            if (!java.util.Objects.equals(existente.getId(), f.getId()))
                throw new IllegalArgumentException(Msg.get("interface.ja.existe.um.fator.para.este.par.de.programas"));
        });
        return repository.saveAndFlush(f);
    }

}

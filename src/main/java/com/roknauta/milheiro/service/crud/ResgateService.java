package com.roknauta.milheiro.service.crud;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.dto.crud.ResgateDTO;
import com.roknauta.milheiro.repository.ResgateRepository;
import com.roknauta.milheiro.service.OperacaoRepositories;
import com.roknauta.milheiro.service.OperacaoService;
import com.roknauta.milheiro.service.mapper.ResgateMapper;
import com.roknauta.milheiro.service.Resumo;
import com.roknauta.milheiro.service.Calculos;
import com.roknauta.milheiro.web.Msg;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResgateService extends OperacaoService implements CrudService<ResgateDTO> {
    private final ResgateMapper mapper;
    private final ResgateRepository resgates;

    public ResgateService(OperacaoRepositories repositories, ResgateMapper mapper, ResgateRepository repository) {
        super(repositories);
        this.mapper = mapper;
        this.resgates = repository;
    }

    private ResgateDTO toDTO(Resgate entity) {
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ResgateDTO findById(Long id) {
        return resgates.findById(id).map(this::toDTO)
                .orElseThrow(() -> erro("crud.registro.nao.encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResgateDTO> findAll() {
        return resgates.findAll().stream()
                .sorted(Comparator.comparing(Resgate::getData).thenComparing(Resgate::getId).reversed())
                .map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResgateDTO> find(ResgateDTO filtro) {
        return findAll().stream().filter(dto -> super.corresponde(filtro, textosPesquisa(dto))).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        super.excluir(findById(id));
    }

    @Override
    @Transactional
    public void delete(ResgateDTO dto) {
        super.excluir(dto);
    }

    @Override
    @Transactional
    public ResgateDTO save(ResgateDTO dto) {
        return toDTO(salvarResgate(dto, dto.getProgramaId()));
    }

    public Resgate salvarResgate(ResgateDTO formulario, Long programaId) {
        Resgate resgate = mapper.toEntity(formulario);
        prepararLancamento(resgate, formulario, programaId);
        resgate.setValor(formulario.getTaxas());
        if (formulario.getId() != null) {
            Resgate existente = resgates.findById(formulario.getId())
                    .orElseThrow(() -> erro("operacao.nao.encontrada"));
            validarEdicao(existente, formulario);
            mapper.updateEntity(formulario, existente);
            existente.setPrograma(resgate.getPrograma());
            existente.setValor(resgate.getValor());
            resgate = existente;
        }
        Resgate salvo = resgates.saveAndFlush(resgate);
        atualizarConsolidados();
        return salvo;
    }

    public static void consolidar(Operacao operacao, Resumo resumo) {
        resumo.saida(operacao.getQuantidade());
    }
}

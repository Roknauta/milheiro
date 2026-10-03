package com.roknauta.milheiro.service.crud;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.dto.crud.VendaDTO;
import com.roknauta.milheiro.repository.VendaRepository;
import com.roknauta.milheiro.service.OperacaoRepositories;
import com.roknauta.milheiro.service.OperacaoService;
import com.roknauta.milheiro.service.mapper.VendaMapper;
import com.roknauta.milheiro.service.Resumo;
import com.roknauta.milheiro.service.Calculos;
import com.roknauta.milheiro.web.Msg;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendaService extends OperacaoService implements CrudService<VendaDTO> {
    private final VendaMapper mapper;
    private final VendaRepository vendas;

    public VendaService(OperacaoRepositories repositories, VendaMapper mapper, VendaRepository repository) {
        super(repositories);
        this.mapper = mapper;
        this.vendas = repository;
    }

    private VendaDTO toDTO(Venda entity) {
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public VendaDTO findById(Long id) {
        return vendas.findById(id).map(this::toDTO)
                .orElseThrow(() -> erro("crud.registro.nao.encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendaDTO> findAll() {
        return vendas.findAll().stream()
                .sorted(Comparator.comparing(Venda::getData).thenComparing(Venda::getId).reversed())
                .map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendaDTO> find(VendaDTO filtro) {
        return findAll().stream().filter(dto -> super.corresponde(filtro, textosPesquisa(dto))).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        super.excluir(findById(id));
    }

    @Override
    @Transactional
    public void delete(VendaDTO dto) {
        super.excluir(dto);
    }

    @Override
    @Transactional
    public VendaDTO save(VendaDTO dto) {
        return toDTO(salvarVenda(dto, dto.getProgramaId()));
    }

    public Venda salvarVenda(VendaDTO formulario, Long programaId) {
        Venda venda = mapper.toEntity(formulario);
        prepararLancamento(venda, formulario, programaId);
        venda.setValor(Dinheiro.de(formulario.getValor().subtract(formulario.getTaxas())));
        if (formulario.getId() != null) {
            Venda existente = vendas.findById(formulario.getId())
                    .orElseThrow(() -> erro("operacao.nao.encontrada"));
            validarEdicao(existente, formulario);
            mapper.updateEntity(formulario, existente);
            existente.setPrograma(venda.getPrograma());
            existente.setValor(venda.getValor());
            venda = existente;
        }
        Venda salvo = vendas.saveAndFlush(venda);
        atualizarConsolidados();
        return salvo;
    }

    public static void consolidar(Operacao operacao, Resumo resumo) {
        resumo.saida(operacao.getQuantidade());
        resumo.setVendas(resumo.getVendas().add(operacao.getValor()));
    }
}

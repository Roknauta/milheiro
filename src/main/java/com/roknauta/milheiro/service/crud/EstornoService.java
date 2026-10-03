package com.roknauta.milheiro.service.crud;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.dto.crud.EstornoDTO;
import com.roknauta.milheiro.repository.EstornoRepository;
import com.roknauta.milheiro.service.OperacaoRepositories;
import com.roknauta.milheiro.service.OperacaoService;
import com.roknauta.milheiro.service.mapper.EstornoMapper;
import com.roknauta.milheiro.service.Resumo;
import com.roknauta.milheiro.service.Calculos;
import com.roknauta.milheiro.web.Msg;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstornoService extends OperacaoService implements CrudService<EstornoDTO> {
    private final EstornoMapper mapper;
    private final EstornoRepository estornos;
    private final com.roknauta.milheiro.service.mapper.OperacaoMapper operacaoMapper;

    public EstornoService(OperacaoRepositories repositories, EstornoMapper mapper, EstornoRepository repository, com.roknauta.milheiro.service.mapper.OperacaoMapper operacaoMapper) {
        super(repositories);
        this.mapper = mapper;
        this.estornos = repository;
        this.operacaoMapper = operacaoMapper;
    }

    private EstornoDTO toDTO(Estorno entity) {
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public EstornoDTO findById(Long id) {
        return estornos.findById(id).map(this::toDTO)
                .orElseThrow(() -> erro("crud.registro.nao.encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstornoDTO> findAll() {
        return estornos.findAll().stream()
                .sorted(Comparator.comparing(Estorno::getData).thenComparing(Estorno::getId).reversed())
                .map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstornoDTO> find(EstornoDTO filtro) {
        return findAll().stream().filter(dto -> super.corresponde(filtro, textosPesquisa(dto))).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        super.excluir(findById(id));
    }

    @Override
    @Transactional
    public void delete(EstornoDTO dto) {
        super.excluir(dto);
    }

    @Override
    @Transactional
    public EstornoDTO save(EstornoDTO dto) {
        return toDTO(salvarEstorno(dto));
    }
    public java.util.stream.Stream<Object> textosPesquisa(EstornoDTO dto) {
        return java.util.stream.Stream.concat(super.textosPesquisa(dto),
                java.util.stream.Stream.of(dto.getOperacaoOriginalId(), dto.getOperacaoOriginal().getTipo().getDescricao()));
    }

    @Transactional(readOnly = true)
    public java.util.List<com.roknauta.milheiro.dto.crud.OperacaoDTO> operacoesEstornaveis(EstornoDTO formulario) {
        if (formulario == null) return java.util.List.of();
        java.util.List<Operacao> historico = operacoes();
        java.util.Set<Long> estornadas = historico.stream()
                .filter(dto -> dto.getTipo() == TipoOperacao.ESTORNO && dto.isConfirmada())
                .filter(dto -> !java.util.Objects.equals(dto.getId(), formulario.getId()))
                .map(dto -> ((Estorno) dto).getOperacaoOriginal().getId())
                .collect(java.util.stream.Collectors.toSet());
        return historico.stream().filter(dto -> dto.isConfirmada() && dto.getTipo() != TipoOperacao.ESTORNO)
                .filter(dto -> !estornadas.contains(dto.getId())).map(operacaoMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public void selecionarOriginal(EstornoDTO dto) {
        var original = operacoes().stream()
                .filter(item -> java.util.Objects.equals(item.getId(), dto.getOperacaoOriginalId())).findFirst().map(operacaoMapper::toDTO);
        dto.setOperacaoOriginal(original.orElse(null));
        dto.setVersaoOriginal(original.map(com.roknauta.milheiro.dto.crud.OperacaoDTO::getVersao).orElse(null));
    }

    public Estorno salvarEstorno(EstornoDTO formulario) {
        iniciarOperacao(formulario);
        Estorno estorno = prepararEstorno(formulario);
        if (formulario.getId() != null) {
            Estorno existente = estornos.findById(formulario.getId())
                    .orElseThrow(() -> erro("operacao.nao.encontrada"));
            validarEdicao(existente, formulario);
            mapper.updateEntity(formulario, existente);
            existente.setPrograma(estorno.getPrograma());
            existente.setQuantidade(estorno.getQuantidade());
            existente.setValor(estorno.getValor());
            existente.setOperacaoOriginal(estorno.getOperacaoOriginal());
            estorno = existente;
        }
        Estorno salvo = estornos.saveAndFlush(estorno);
        atualizarConsolidados();
        return salvo;
    }

    private Estorno prepararEstorno(EstornoDTO f) {
        if (f.getOperacaoOriginalId() == null) throw erro("estorno.selecione.operacao");
        Operacao original = operacoes.findById(f.getOperacaoOriginalId())
                .orElseThrow(() -> erro("estorno.selecione.operacao"));
        if (!Objects.equals(f.getVersaoOriginal(), original.getVersao()))
            throw new org.springframework.dao.OptimisticLockingFailureException("Operacao " + original.getId());
        if (!original.isConfirmada() || original instanceof Estorno || possuiOutroEstorno(original.getId(), f.getId()))
            throw erro("estorno.operacao.invalida");
        if (f.getData().isBefore(original.getData())) throw erro("estorno.data.invalida");
        Estorno estorno = mapper.toEntity(f);
        estorno.setOperacaoOriginal(original);
        estorno.setPrograma(original.getPrograma());
        estorno.setQuantidade(original.getQuantidade());
        estorno.setValor(original.getValor());
        return estorno;
    }

    private boolean possuiOutroEstorno(Long originalId, Long ignorarId) {
        return estornos.findAll().stream().anyMatch(e -> e.isConfirmada()
                && !Objects.equals(e.getId(), ignorarId)
                && Objects.equals(e.getOperacaoOriginal().getId(), originalId));
    }

    public static void consolidar(Operacao operacao, Resumo resumo, Set<Long> estornadas) {
        Estorno estorno = (Estorno) operacao;
        Operacao original = estorno.getOperacaoOriginal();
        if (!original.isConfirmada() || original instanceof Estorno
                || !estornadas.add(original.getId()) || estorno.getData().isBefore(original.getData()))
            throw new IllegalArgumentException(Msg.get("estorno.operacao.invalida"));
        if (original instanceof Acumulo) {
            resumo.saida(estorno.getQuantidade());
            resumo.setAcumulado(resumo.getAcumulado().subtract(estorno.getQuantidade()));
            resumo.setGasto(resumo.getGasto().subtract(estorno.getValor()));
            return;
        }
        resumo.setSaldo(resumo.getSaldo().add(estorno.getQuantidade()));
        if (original instanceof Venda)
            resumo.setVendas(resumo.getVendas().subtract(estorno.getValor()));
    }
}

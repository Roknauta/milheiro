package com.roknauta.milheiro.service.crud;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.dto.crud.AcumuloDTO;
import com.roknauta.milheiro.repository.AcumuloRepository;
import com.roknauta.milheiro.service.OperacaoRepositories;
import com.roknauta.milheiro.service.OperacaoService;
import com.roknauta.milheiro.service.mapper.AcumuloMapper;
import com.roknauta.milheiro.service.Resumo;
import com.roknauta.milheiro.service.Calculos;
import com.roknauta.milheiro.web.Msg;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcumuloService extends OperacaoService implements CrudService<AcumuloDTO> {
    private final AcumuloMapper mapper;
    private final AcumuloRepository acumulos;

    public AcumuloService(OperacaoRepositories repositories, AcumuloMapper mapper, AcumuloRepository repository) {
        super(repositories);
        this.mapper = mapper;
        this.acumulos = repository;
    }

    private AcumuloDTO toDTO(Acumulo entity) {
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public AcumuloDTO findById(Long id) {
        return acumulos.findById(id).map(this::toDTO)
                .orElseThrow(() -> erro("crud.registro.nao.encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcumuloDTO> findAll() {
        return acumulos.findAll().stream()
                .sorted(Comparator.comparing(Acumulo::getData).thenComparing(Acumulo::getId).reversed())
                .map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcumuloDTO> find(AcumuloDTO filtro) {
        return findAll().stream().filter(dto -> super.corresponde(filtro, textosPesquisa(dto))).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        super.excluir(findById(id));
    }

    @Override
    @Transactional
    public void delete(AcumuloDTO dto) {
        super.excluir(dto);
    }

    @Override
    @Transactional
    public AcumuloDTO save(AcumuloDTO dto) {
        return toDTO(salvarAcumulo(dto, dto.getProgramaId()));
    }

    public java.util.stream.Stream<Object> textosPesquisa(AcumuloDTO dto) {
        return java.util.stream.Stream.concat(super.textosPesquisa(dto),
                java.util.stream.Stream.of(dto.getVinculoTransferencia(), dto.getParcelaDescricao()));
    }


    public Acumulo salvarAcumulo(AcumuloDTO formulario, Long programaId) {
        Acumulo acumulo = mapper.toEntity(formulario);
        prepararLancamento(acumulo, formulario, programaId);
        acumulo.setValor(Dinheiro.de(formulario.getValor().add(formulario.getTaxas())));
        if (formulario.getId() != null) {
            Acumulo existente = acumulos.findById(formulario.getId())
                    .orElseThrow(() -> erro("operacao.nao.encontrada"));
            validarEdicao(existente, formulario);
            mapper.updateEntity(formulario, existente);
            existente.setPrograma(acumulo.getPrograma());
            existente.setValor(acumulo.getValor());
            acumulo = existente;
        }
        validarCreditoTransferencia(acumulo);
        Acumulo salvo = acumulos.saveAndFlush(acumulo);
        atualizarConsolidados();
        return salvo;
    }

    private void validarCreditoTransferencia(Acumulo credito) {
        if (!credito.isCreditoTransferencia()) return;
        if (!Objects.equals(credito.getPrograma().getId(), credito.getTransferenciaOrigem().getDestino().getId()))
            throw erro("operacao.credito.programa.invalido");
        if (credito.getParcelaTransferencia() == ParcelaTransferencia.BONUS && credito.getValor().signum() != 0)
            throw erro("operacao.bonus.valor.invalido");
    }

    public void confirmarAcumulo(Long id, Long versao) {
        bloquearOperacoes();
        if (!(carregarParaAlterar(id, versao) instanceof Acumulo)) throw erro("operacao.tipo.invalido");
        alterarStatus(id, versao, StatusOperacao.CONFIRMADO);
    }

    public void criarCredito(Transferencia origem, ParcelaTransferencia parcela,
                              BigDecimal pontos, BigDecimal custo) {
        Acumulo credito = mapper.toCredito(origem, parcela, pontos, Dinheiro.de(custo));
        acumulos.saveAndFlush(credito);
    }

    public void confirmar(AcumuloDTO dto) {
        confirmarAcumulo(dto.getId(), dto.getVersao());
    }

    public static void consolidar(Operacao operacao, Resumo resumo) {
        resumo.entrada(operacao.getQuantidade(), operacao.getValor());
    }
}

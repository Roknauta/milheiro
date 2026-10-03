package com.roknauta.milheiro.service.crud;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.dto.crud.TransferenciaDTO;
import com.roknauta.milheiro.repository.TransferenciaRepository;
import com.roknauta.milheiro.repository.AcumuloRepository;
import com.roknauta.milheiro.dto.ResultadoTransferencia;
import com.roknauta.milheiro.helper.TransferenciaHelper;
import com.roknauta.milheiro.service.OperacaoRepositories;
import com.roknauta.milheiro.service.OperacaoService;
import com.roknauta.milheiro.service.mapper.TransferenciaMapper;
import com.roknauta.milheiro.service.Resumo;
import com.roknauta.milheiro.service.Calculos;
import com.roknauta.milheiro.web.Msg;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferenciaService extends OperacaoService implements CrudService<TransferenciaDTO> {
    private final TransferenciaMapper mapper;
    private final TransferenciaRepository transferencias;
    private final AcumuloRepository creditos;
    private final AcumuloService acumuloService;
    private final ProgramaFidelidadeService programas;
    private final FatorConversaoService fatores;
    private final com.roknauta.milheiro.service.mapper.FatorConversaoMapper fatorMapper;

    public TransferenciaService(OperacaoRepositories repositories, TransferenciaMapper mapper, TransferenciaRepository repository, com.roknauta.milheiro.service.mapper.FatorConversaoMapper fatorMapper, ProgramaFidelidadeService programas, FatorConversaoService fatores, AcumuloRepository creditos, AcumuloService acumuloService) {
        super(repositories);
        this.mapper = mapper;
        this.transferencias = repository;
        this.programas = programas;
        this.fatores = fatores;
        this.fatorMapper = fatorMapper;
        this.creditos = creditos;
        this.acumuloService = acumuloService;
    }

    private TransferenciaDTO toDTO(Transferencia entity) {
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferenciaDTO findById(Long id) {
        return transferencias.findById(id).map(this::toDTO)
                .orElseThrow(() -> erro("crud.registro.nao.encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferenciaDTO> findAll() {
        return transferencias.findAll().stream()
                .sorted(Comparator.comparing(Transferencia::getData).thenComparing(Transferencia::getId).reversed())
                .map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferenciaDTO> find(TransferenciaDTO filtro) {
        return findAll().stream().filter(dto -> super.corresponde(filtro, textosPesquisa(dto))).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        super.excluir(findById(id));
    }

    @Override
    @Transactional
    public void delete(TransferenciaDTO dto) {
        super.excluir(dto);
    }

    @Override
    @Transactional
    public TransferenciaDTO save(TransferenciaDTO dto) {
        return toDTO(salvarTransferencia(dto, dto.getProgramaId(), dto.getDestinoId()));
    }

    public java.util.stream.Stream<Object> textosPesquisa(TransferenciaDTO dto) {
        return java.util.stream.Stream.concat(super.textosPesquisa(dto),
                java.util.stream.Stream.of(dto.getDestino().getNome()));
    }

    @Transactional(readOnly = true)
    public void aplicarFator(TransferenciaDTO dto) {
        fatorCadastrado(dto).ifPresentOrElse(fator -> {
            dto.setFatorCadastrado(fatorMapper.toDTO(fator));
            dto.setConversaoExcepcional(false);
            dto.setPontosOrigem(fator.getPontosOrigem());
            dto.setPontosDestino(fator.getPontosDestino());
        }, () -> {
            dto.setFatorCadastrado(null);
            dto.setConversaoExcepcional(true);
            dto.setPontosOrigem(java.math.BigDecimal.ONE);
            dto.setPontosDestino(java.math.BigDecimal.ONE);
        });
        dto.setBonus(java.math.BigDecimal.ZERO);
        atualizarResumo(dto);
    }

    private java.util.Optional<FatorConversao> fatorCadastrado(TransferenciaDTO dto) {
        return fatores.listar().stream().filter(fator ->
                java.util.Objects.equals(fator.getOrigem().getId(), dto.getProgramaId())
                && java.util.Objects.equals(fator.getDestino().getId(), dto.getDestinoId())).findFirst();
    }

    @Transactional(readOnly = true)
    public void alternarConversaoExcepcional(TransferenciaDTO dto) {
        if (!dto.isConversaoExcepcional()) {
            java.math.BigDecimal bonus = dto.getBonus();
            aplicarFator(dto);
            dto.setBonus(bonus);
        }
        atualizarResumo(dto);
    }

    private void limparResumo(TransferenciaDTO dto) {
        dto.setResultadoTransferencia(null);
        dto.setResumoPontos(null);
        dto.setResumoPontosCarrinho(null);
        dto.setResumoCusto(null);
        dto.setResumoTotal(null);
        dto.setResumoEuros(null);
        dto.setResumoMilheiroCarrinho(null);
        dto.setResumoAviso(null);
        dto.setCustoPorEuro(null);
        dto.setPontosDebitados(null);
        dto.setPontosComprados(null);
        dto.setCustoCarrinho(dto.isComCarrinho() ? dto.getValorCarrinho() : Dinheiro.ZERO);
    }

    @Transactional(readOnly = true)
    public void atualizarResumo(TransferenciaDTO dto) {
        if (dto == null) return;
        limparResumo(dto);
        if (dto.getData() == null || dto.getProgramaId() == null || dto.getDestinoId() == null) return;
        try {
            if (java.util.Objects.equals(dto.getProgramaId(), dto.getDestinoId()))
                throw new IllegalArgumentException(com.roknauta.milheiro.web.Msg.get("interface.origem.e.destino.devem.ser.diferentes"));
            var origem = resumoAntesDaOperacao(dto.getData(), dto.getProgramaId());
            if (origem == null) return;
            var resultado = com.roknauta.milheiro.helper.TransferenciaHelper.calcular(dto.parametros(), origem,
                    false, 2, java.math.RoundingMode.DOWN);
            dto.setResultadoTransferencia(resultado);
            dto.setResumoPontos(resultado.creditos());
            dto.setResumoCusto(resultado.custoOrigem());
            dto.setResumoTotal(resultado.custoTotal());
            dto.setResumoPontosCarrinho(resultado.creditosCarrinho());
            dto.setResumoMilheiroCarrinho(resultado.milheiroCarrinho());
            dto.setPontosDebitados(resultado.debito());
            dto.setPontosComprados(resultado.comprados());
            programas.listar().stream().filter(programa -> java.util.Objects.equals(programa.getId(), dto.getDestinoId()))
                    .filter(ProgramaFidelidade::isAll).findFirst().ifPresent(programa ->
                            dto.setResumoEuros(com.roknauta.milheiro.service.Calculos.eurosAll(resultado.creditos())));
            dto.setCustoPorEuro(com.roknauta.milheiro.helper.TransferenciaHelper.custoPorEuro(
                    dto.getResumoTotal(), dto.getResumoEuros()));
            if (resultado.debito().compareTo(origem.getSaldo()) > 0)
                dto.setResumoAviso(com.roknauta.milheiro.web.Msg.get("transferencia.saldo.insuficiente"));
        } catch (IllegalArgumentException e) {
            dto.setResumoAviso(e.getMessage());
        }
    }

    public Transferencia salvarTransferencia(TransferenciaDTO formulario, Long programaId, Long destinoId) {
        if (formulario.getId() != null) return editarTransferencia(formulario, programaId, destinoId);
        Transferencia transferencia = mapper.toEntity(formulario);
        prepararLancamento(transferencia, formulario, programaId);
        ProgramaFidelidade destino = programa(destinoId);
        ativo(destino);
        if (Objects.equals(programaId, destinoId))
            throw erro("interface.origem.e.destino.devem.ser.diferentes");
        if (formulario.isComCarrinho()) {
            validarPrecisao(formulario.getPontosDebitarSaldo());
            validarPrecisao(formulario.getValorCarrinho());
        }
        ResultadoTransferencia resultado = TransferenciaHelper.calcular(formulario.parametros(),
                resumoAntesDaOperacao(formulario.getData(), programaId), false, 2, RoundingMode.DOWN);
        if (resultado.creditos().signum() <= 0)
            throw erro("interface.a.transferencia.precisa.gerar.pelo.menos.0.01.ponto");
        transferencia.setDestino(destino);
        transferencia.setQuantidade(resultado.debito());
        transferencia.setValor(Dinheiro.de(resultado.custoTotal().setScale(2, RoundingMode.HALF_UP)));
        transferencia.setValorAdicional(resultado.valorAdicional());
        transferencia.setStatus(StatusOperacao.PENDENTE);
        transferencias.saveAndFlush(transferencia);
        acumuloService.criarCredito(transferencia, ParcelaTransferencia.BASE, resultado.base(), transferencia.getValor());
        if (resultado.bonus().signum() > 0)
            acumuloService.criarCredito(transferencia, ParcelaTransferencia.BONUS, resultado.bonus(), BigDecimal.ZERO);
        atualizarConsolidados();
        return transferencia;
    }

    private Transferencia editarTransferencia(TransferenciaDTO formulario, Long programaId, Long destinoId) {
        iniciarOperacao(formulario);
        Operacao registro = carregarParaAlterar(formulario.getId(), formulario.getVersao());
        if (!(registro instanceof Transferencia transferencia)) throw erro("operacao.tipo.invalido");
        verificarEstornosVinculados(registro.getId());
        ProgramaFidelidade origem = programa(programaId);
        ProgramaFidelidade destino = programa(destinoId);
        ativo(origem);
        ativo(destino);
        if (Objects.equals(programaId, destinoId)) throw erro("interface.origem.e.destino.devem.ser.diferentes");
        Calculos.naoNegativo(formulario.getQuantidade(), Msg.get("campo.quantidade"));
        Calculos.naoNegativo(formulario.getValor(), Msg.get("campo.valor"));
        Calculos.naoNegativo(formulario.getValorAdicional(), Msg.get("campo.valor"));
        validarPrecisao(formulario.getQuantidade());
        validarPrecisao(formulario.getValor());
        validarPrecisao(formulario.getValorAdicional());
        if (formulario.getValorAdicional().compareTo(formulario.getValor()) > 0)
            throw erro("operacao.adicional.invalido");
        mapper.updateEntity(formulario, transferencia);
        transferencia.setPrograma(origem);
        transferencia.setDestino(destino);
        for (Operacao o : operacoes.findAll()) {
            if (o instanceof Acumulo credito && credito.isCreditoTransferencia()
                    && Objects.equals(credito.getTransferenciaOrigem().getId(), transferencia.getId())) {
                verificarEstornosVinculados(credito.getId());
                credito.setPrograma(destino);
                credito.setData(formulario.getData());
                credito.setValor(credito.getParcelaTransferencia() == ParcelaTransferencia.BONUS
                        ? Dinheiro.ZERO : transferencia.getValor());
            }
        }
        Transferencia salva = transferencias.saveAndFlush(transferencia);
        atualizarConsolidados();
        return salva;
    }

    public void confirmarTransferenciaImportada(Long id) {
        bloquearOperacoes();
        for (Operacao o : operacoes.findAllByOrderByDataAscIdAsc()) {
            if (Objects.equals(o.getId(), id) || (o.isCreditoTransferencia()
                    && Objects.equals(o.getTransferenciaOrigem().getId(), id)))
                o.setStatus(StatusOperacao.CONFIRMADO);
        }
        operacoes.flush();
        atualizarConsolidados();
    }

    @Override
    public void excluirDependencias(Operacao operacao) {
        List<Acumulo> vinculados = creditos.findAll().stream()
                .filter(credito -> Objects.equals(credito.getVinculoTransferencia(), operacao.getId())).toList();
        vinculados.forEach(credito -> verificarEstornosVinculados(credito.getId()));
        creditos.deleteAll(vinculados);
        creditos.flush();
    }

    public static void consolidar(Operacao operacao, Resumo resumo) {
        resumo.saida(operacao.getQuantidade());
    }
}

package com.roknauta.milheiro.service;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.dto.crud.OperacaoDTO;
import com.roknauta.milheiro.service.crud.*;
import com.roknauta.milheiro.repository.ConsolidadoRepository;
import com.roknauta.milheiro.web.Msg;
import com.roknauta.milheiro.repository.OperacaoRepository;
import com.roknauta.milheiro.repository.ProgramaFidelidadeRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Primary
@Transactional
public class OperacaoService {

    private final ProgramaFidelidadeRepository programas;
    protected final OperacaoRepository operacoes;
    private final ConsolidadoRepository consolidados;
    private final com.roknauta.milheiro.service.mapper.ConsolidadoMapper consolidadoMapper;

    public OperacaoService(OperacaoRepositories repositories) {
        this.programas = repositories.programas();
        this.operacoes = repositories.operacoes();
        this.consolidados = repositories.consolidados();
        this.consolidadoMapper = repositories.consolidadoMapper();
    }

    @Transactional(readOnly = true)
    public List<Operacao> operacoes() {
        var lista = operacoes.findAllByOrderByDataAscIdAsc();
        reconstruir(lista);
        Collections.reverse(lista);
        return lista;
    }

    public java.util.stream.Stream<Object> textosPesquisa(OperacaoDTO dto) {
        return java.util.stream.Stream.of(dto.getId(), dto.getPrograma().getNome(),
                dto.getStatus().getDescricao(), dto.getObservacoes());
    }

    public boolean corresponde(OperacaoDTO filtro, java.util.stream.Stream<Object> textos) {
        String termo = Optional.ofNullable(filtro.getFiltro()).orElse("").trim().toLowerCase(Locale.ROOT);
        return textos.filter(Objects::nonNull).map(Object::toString)
                .anyMatch(texto -> texto.toLowerCase(Locale.ROOT).contains(termo));
    }

    public ProgramaFidelidade programa(Long id) {
        if (id == null)
            throw new IllegalArgumentException(Msg.get("interface.selecione.o.programa.2"));
        return programas.findById(id).orElseThrow(() -> new IllegalArgumentException(Msg.get("interface.programa.nao.encontrado")));
    }

    public void ativo(ProgramaFidelidade p) {
        if (!p.isAtivo())
            throw new IllegalArgumentException(Msg.get("interface.o.programa") + " " + p.getNome() + " " + Msg.get("interface.esta.inativo"));
    }

    public void bloquearOperacoes() { programas.bloquearTodos(); }

    public void iniciarOperacao(OperacaoDTO formulario) {
        programas.bloquearTodos();
        if (formulario.getData() == null) throw erro("interface.informe.data.e.tipo");
        if (formulario.getObservacoes() != null && formulario.getObservacoes().length() > 500)
            throw erro("interface.observacoes.maximo.de.500.caracteres");
    }

    public void prepararLancamento(
            Operacao entidade, OperacaoDTO formulario, Long programaId) {
        iniciarOperacao(formulario);
        ProgramaFidelidade programa = programa(programaId);
        ativo(programa);
        Calculos.positivo(formulario.getQuantidade(), Msg.get("campo.quantidade"));
        validarPrecisao(formulario.getQuantidade());
        Calculos.naoNegativo(formulario.getValor(), Msg.get("campo.valor"));
        Calculos.naoNegativo(formulario.getTaxas(), Msg.get("campo.taxas"));
        validarPrecisao(formulario.getValor());
        validarPrecisao(formulario.getTaxas());
        entidade.setPrograma(programa);
    }

    public void validarEdicao(Operacao existente, OperacaoDTO formulario) {
        if (!Objects.equals(formulario.getVersao(), existente.getVersao()))
            throw new org.springframework.dao.OptimisticLockingFailureException("Operacao " + existente.getId());
        verificarEstornosVinculados(existente.getId());
    }

    public Operacao carregarParaAlterar(Long id, Long versao) {
        Operacao operacao = operacoes.findById(id).orElseThrow(() -> erro("operacao.nao.encontrada"));
        if (!Objects.equals(versao, operacao.getVersao()))
            throw new org.springframework.dao.OptimisticLockingFailureException("Operacao " + id);
        return operacao;
    }

    public void verificarEstornosVinculados(Long id) {
        if (operacoes.possuiEstornoVinculado(id))
            throw erro("operacao.estorno.vinculado");
    }

    public void excluirOperacao(Long id, Long versao) {
        programas.bloquearTodos();
        Operacao operacao = carregarParaAlterar(id, versao);
        verificarEstornosVinculados(id);
        excluirDependencias(operacao);
        operacoes.delete(operacao);
        operacoes.flush();
        atualizarConsolidados();
    }

    public void excluirDependencias(Operacao operacao) { }

    public void excluir(OperacaoDTO dto) {
        excluirOperacao(dto.getId(), dto.getVersao());
    }

    public void cancelar(OperacaoDTO dto) {
        cancelarOperacao(dto.getId(), dto.getVersao());
    }

    public void cancelarOperacao(Long id, Long versao) {
        alterarStatus(id, versao, StatusOperacao.CANCELADO);
    }

    private boolean possuiEstorno(Long id) {
        return operacoes.possuiEstornoConfirmado(id, StatusOperacao.CONFIRMADO);
    }

    public void validarPrecisao(BigDecimal valor) {
        if (valor == null || valor.stripTrailingZeros().scale() > 2) throw erro("operacao.precisao.invalida");
    }

    public IllegalArgumentException erro(String chave) {
        return new IllegalArgumentException(Msg.get(chave));
    }

    public void alterarStatus(Long id, Long versao, StatusOperacao novoStatus) {
        programas.bloquearTodos();
        Operacao o = operacoes.findById(id).orElseThrow();
        if (!Objects.equals(versao, o.getVersao()))
            throw new org.springframework.dao.OptimisticLockingFailureException("Operacao " + id);
        if (novoStatus == null || novoStatus == o.getStatus()) throw erro("operacao.status.invalido");
        if (possuiEstorno(id)) throw erro("estorno.original.bloqueada");
        o.setStatus(novoStatus);
        operacoes.saveAndFlush(o);
        atualizarConsolidados();
    }

    /**
     * Participa da transação da importação; a limpeza é revertida se houver falha.
     */
    public void limparHistoricoParaImportacao() {
        programas.bloquearTodos();
        operacoes.excluirEstornos();
        operacoes.excluirAcumulos();
        operacoes.excluirHistorico();
        atualizarConsolidados();
    }

    /**
     * Histórico e posição consolidada são gravados na mesma transação.
     */
    public void atualizarConsolidados() {
        programas.bloquearTodos();
        reconstruir().values().forEach(r -> {
            Consolidado c = consolidados.findById(r.getPrograma().getId())
                    .orElseGet(() -> consolidadoMapper.toEntity(r));
            consolidadoMapper.updateEntity(r, c);
            consolidados.save(c);
        });
        consolidados.flush();
    }

    @Transactional(readOnly = true)
    public List<Consolidado> consolidados() {
        return consolidados.findAll();
    }

    @Transactional(readOnly = true)
    public List<Resumo> resumos() {
        return new ArrayList<>(reconstruir().values());
    }

    @Transactional(readOnly = true)
    public Resumo resumoAntesDaOperacao(java.time.LocalDate data, Long origem) {
        return reconstruir(operacoes.findAllByOrderByDataAscIdAsc().stream()
                .filter(o -> !o.getData().isAfter(data)).toList()).get(origem);
    }

    private Map<Long, Resumo> reconstruir() {
        return reconstruir(operacoes.findAllByOrderByDataAscIdAsc());
    }

    private Map<Long, Resumo> reconstruir(List<Operacao> historico) {
        Map<Long, Resumo> mapa = new LinkedHashMap<>();
        programas.findAllByOrderByNomeAsc().forEach(p -> mapa.put(p.getId(), new Resumo(p)));
        var ordenado = new ArrayList<>(historico);
        ordenado.sort(Comparator.comparing(Operacao::getData)
                .thenComparing(o -> o.isCreditoTransferencia() ? o.getTransferenciaOrigem().getId() : o.getId())
                .thenComparing(Operacao::getId));
        Set<Long> estornadas = new HashSet<>();
        Map<TipoOperacao, java.util.function.BiConsumer<Operacao, Resumo>> tratamentos = new EnumMap<>(TipoOperacao.class);
        tratamentos.put(TipoOperacao.ACUMULO, AcumuloService::consolidar);
        tratamentos.put(TipoOperacao.VENDA, VendaService::consolidar);
        tratamentos.put(TipoOperacao.RESGATE, ResgateService::consolidar);
        tratamentos.put(TipoOperacao.TRANSFERENCIA, TransferenciaService::consolidar);
        tratamentos.put(TipoOperacao.ESTORNO, (operacao, resumo) -> EstornoService.consolidar(operacao, resumo, estornadas));
        for (Operacao operacao : ordenado) {
            if (!operacao.isConfirmada()) continue;
            tratamentos.get(operacao.getTipo()).accept(operacao, mapa.get(operacao.getPrograma().getId()));
        }
        return mapa;
    }

}

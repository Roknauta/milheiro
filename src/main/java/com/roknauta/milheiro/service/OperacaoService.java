package com.roknauta.milheiro.service;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.dto.*;
import com.roknauta.milheiro.helper.TransferenciaHelper;
import com.roknauta.milheiro.repository.AcumuloRepository;
import com.roknauta.milheiro.repository.VendaRepository;
import com.roknauta.milheiro.repository.ResgateRepository;
import com.roknauta.milheiro.repository.TransferenciaRepository;
import com.roknauta.milheiro.repository.EstornoRepository;
import com.roknauta.milheiro.repository.ConsolidadoRepository;
import com.roknauta.milheiro.web.Textos;
import com.roknauta.milheiro.repository.FatorRepository;
import com.roknauta.milheiro.repository.OperacaoRepository;
import com.roknauta.milheiro.repository.ProgramaFidelidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Transactional
public class OperacaoService {

    private final ProgramaFidelidadeRepository programas;
    private final FatorRepository fatores;
    private final OperacaoRepository operacoes;
    private final ConsolidadoRepository consolidados;
    private final AcumuloRepository acumulos;
    private final VendaRepository vendas;
    private final ResgateRepository resgates;
    private final TransferenciaRepository transferencias;
    private final EstornoRepository estornos;


    public OperacaoService(ProgramaFidelidadeRepository programas, FatorRepository fatores, OperacaoRepository operacoes, ConsolidadoRepository consolidados,
        AcumuloRepository acumulos, VendaRepository vendas, ResgateRepository resgates,
        TransferenciaRepository transferencias, EstornoRepository estornos) {
        this.programas = programas;
        this.fatores = fatores;
        this.operacoes = operacoes;
        this.consolidados = consolidados;
        this.acumulos = acumulos;
        this.vendas = vendas;
        this.resgates = resgates;
        this.transferencias = transferencias;
        this.estornos = estornos;

    }

    @Transactional(readOnly = true)
    public List<ProgramaFidelidade> programas() {
        return programas.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<FatorConversao> fatores() {
        return fatores.findAll();
    }

    @Transactional(readOnly = true)
    public List<Operacao> operacoes() {
        var lista = operacoes.findAllByOrderByDataAscIdAsc();
        reconstruir(lista);
        Collections.reverse(lista);
        return lista;
    }

    public ProgramaFidelidade salvarProgramaFidelidade(ProgramaFidelidade p) {
        programas.bloquearTodos();
        if (p.getNome() == null || p.getNome().trim().isEmpty() || p.getNome().trim().length() > 80)
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.informe.um.nome.de.ate.80.caracteres"));
        p.setNome(p.getNome().trim());
        if (programas.existsByNomeIgnoreCaseAndIdNot(p.getNome(), p.getId() == null ? -1L : p.getId()))
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.ja.existe.um.programa.com.esse.nome"));
        if (p.getCategoria() == null)
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.categoria.invalida"));
        ProgramaFidelidade salvo = programas.saveAndFlush(p);
        atualizarConsolidados();
        return salvo;
    }

    public void excluirProgramaFidelidade(Long id) {
        programas.bloquearTodos();
        if (operacoes.findAll().stream().anyMatch(o -> Objects.equals(o.getPrograma().getId(), id)
            || (o.getDestino() != null && Objects.equals(o.getDestino().getId(), id))) || fatores.existsByOrigemIdOrDestinoId(id, id))
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.programa.em.uso.desative.o.ou.remova.os.fatores.antes.de.excluir"));
        consolidados.deleteById(id);
        consolidados.flush();
        programas.deleteById(id);
    }

    public FatorConversao salvarFator(FatorConversao f, Long origem, Long destino) {
        if (Objects.equals(origem, destino))
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.origem.e.destino.devem.ser.diferentes"));
        f.setOrigem(programa(origem));
        f.setDestino(programa(destino));
        Calculos.positivo(f.getPontosOrigem(), com.roknauta.milheiro.web.Textos.get("interface.pontos.de.origem"));
        Calculos.positivo(f.getPontosDestino(), com.roknauta.milheiro.web.Textos.get("interface.pontos.de.destino"));
        fatores.findByOrigemIdAndDestinoId(origem, destino).ifPresent(existente -> {
            if (!Objects.equals(existente.getId(), f.getId()))
                throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.ja.existe.um.fator.para.este.par.de.programas"));
        });
        return fatores.saveAndFlush(f);
    }

    public void excluirFator(Long id) {
        fatores.deleteById(id);
    }

    private ProgramaFidelidade programa(Long id) {
        if (id == null)
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.selecione.o.programa.2"));
        return programas.findById(id).orElseThrow(() -> new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.programa.nao.encontrado")));
    }

    private void ativo(ProgramaFidelidade p) {
        if (!p.isAtivo())
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.o.programa") + " " + p.getNome() + " " + com.roknauta.milheiro.web.Textos.get("interface.esta.inativo"));
    }

    public Acumulo salvarAcumulo(OperacaoFormulario formulario, Long programaId) {
        var builder = Acumulo.builder();
        prepararLancamento(builder, formulario, programaId);
        Acumulo acumulo = builder.valor(Dinheiro.de(formulario.getValor().add(formulario.getTaxas()))).build();
        if (formulario.getId() != null) {
            Acumulo existente = acumulos.findById(formulario.getId())
                .orElseThrow(() -> erro("operacao.nao.encontrada"));
            prepararEdicao(existente, acumulo, formulario);
            acumulo = existente;
        }
        validarCreditoTransferencia(acumulo);
        Acumulo salvo = acumulos.saveAndFlush(acumulo);
        atualizarConsolidados();
        return salvo;
    }

    public Venda salvarVenda(OperacaoFormulario formulario, Long programaId) {
        var builder = Venda.builder();
        prepararLancamento(builder, formulario, programaId);
        Venda venda = builder.valor(Dinheiro.de(formulario.getValor().subtract(formulario.getTaxas()))).build();
        if (formulario.getId() != null) {
            Venda existente = vendas.findById(formulario.getId())
                .orElseThrow(() -> erro("operacao.nao.encontrada"));
            prepararEdicao(existente, venda, formulario);
            venda = existente;
        }
        Venda salvo = vendas.saveAndFlush(venda);
        atualizarConsolidados();
        return salvo;
    }

    public Resgate salvarResgate(OperacaoFormulario formulario, Long programaId) {
        var builder = Resgate.builder();
        prepararLancamento(builder, formulario, programaId);
        Resgate resgate = builder.valor(formulario.getTaxas()).build();
        if (formulario.getId() != null) {
            Resgate existente = resgates.findById(formulario.getId())
                .orElseThrow(() -> erro("operacao.nao.encontrada"));
            prepararEdicao(existente, resgate, formulario);
            resgate = existente;
        }
        Resgate salvo = resgates.saveAndFlush(resgate);
        atualizarConsolidados();
        return salvo;
    }

    public Transferencia salvarTransferencia(OperacaoFormulario formulario, Long programaId, Long destinoId) {
        if (formulario.getId() != null) return editarTransferencia(formulario, programaId, destinoId);
        var builder = Transferencia.builder();
        prepararLancamento(builder, formulario, programaId);
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
        Transferencia transferencia = builder
            .destino(destino)
            .quantidade(resultado.debito())
            .valor(Dinheiro.de(resultado.custoTotal().setScale(2, RoundingMode.HALF_UP)))
            .valorAdicional(resultado.valorAdicional())
            .status(StatusOperacao.PENDENTE)
            .build();
        transferencias.saveAndFlush(transferencia);
        criarCredito(transferencia, ParcelaTransferencia.BASE, resultado.base(), transferencia.getValor());
        if (resultado.bonus().signum() > 0)
            criarCredito(transferencia, ParcelaTransferencia.BONUS, resultado.bonus(), BigDecimal.ZERO);
        atualizarConsolidados();
        return transferencia;
    }

    public Estorno salvarEstorno(OperacaoFormulario formulario) {
        iniciarOperacao(formulario);
        Estorno estorno = prepararEstorno(formulario);
        if (formulario.getId() != null) {
            Estorno existente = estornos.findById(formulario.getId())
                .orElseThrow(() -> erro("operacao.nao.encontrada"));
            prepararEdicao(existente, estorno, formulario);
            existente.setOperacaoOriginal(estorno.getOperacaoOriginal());
            estorno = existente;
        }
        Estorno salvo = estornos.saveAndFlush(estorno);
        atualizarConsolidados();
        return salvo;
    }

    private void iniciarOperacao(OperacaoFormulario formulario) {
        programas.bloquearTodos();
        if (formulario.getData() == null) throw erro("interface.informe.data.e.tipo");
        if (formulario.getObservacoes() != null && formulario.getObservacoes().length() > 500)
            throw erro("interface.observacoes.maximo.de.500.caracteres");
    }

    private void prepararLancamento(
        Operacao.OperacaoBuilder<?, ?> builder, OperacaoFormulario formulario, Long programaId) {
        iniciarOperacao(formulario);
        ProgramaFidelidade programa = programa(programaId);
        ativo(programa);
        Calculos.positivo(formulario.getQuantidade(), Textos.get("campo.quantidade"));
        validarPrecisao(formulario.getQuantidade());
        Calculos.naoNegativo(formulario.getValor(), Textos.get("campo.valor"));
        Calculos.naoNegativo(formulario.getTaxas(), Textos.get("campo.taxas"));
        validarPrecisao(formulario.getValor());
        validarPrecisao(formulario.getTaxas());
        builder.programa(programa)
            .quantidade(formulario.getQuantidade())
            .valor(formulario.getValor());
        preencherDadosComuns(builder, formulario);
    }

    private void preencherDadosComuns(
        Operacao.OperacaoBuilder<?, ?> builder, OperacaoFormulario formulario) {
        builder.data(formulario.getData())
            .observacoes(formulario.getObservacoes());
    }

    private void prepararEdicao(Operacao existente, Operacao novosDados, OperacaoFormulario formulario) {
        if (!Objects.equals(formulario.getVersao(), existente.getVersao()))
            throw new org.springframework.dao.OptimisticLockingFailureException("Operacao " + existente.getId());
        verificarEstornosVinculados(existente.getId());
        existente.setData(novosDados.getData());
        existente.setPrograma(novosDados.getPrograma());
        existente.setQuantidade(novosDados.getQuantidade());
        existente.setValor(novosDados.getValor());
        existente.setObservacoes(novosDados.getObservacoes());
    }

    private void validarCreditoTransferencia(Acumulo credito) {
        if (!credito.isCreditoTransferencia()) return;
        if (!Objects.equals(credito.getPrograma().getId(), credito.getTransferenciaOrigem().getDestino().getId()))
            throw erro("operacao.credito.programa.invalido");
        if (credito.getParcelaTransferencia() == ParcelaTransferencia.BONUS && credito.getValor().signum() != 0)
            throw erro("operacao.bonus.valor.invalido");
    }

    private Estorno prepararEstorno(OperacaoFormulario f) {
        if (f.getOperacaoOriginalId() == null) throw erro("estorno.selecione.operacao");
        Operacao original = operacoes.findById(f.getOperacaoOriginalId())
            .orElseThrow(() -> erro("estorno.selecione.operacao"));
        if (!Objects.equals(f.getVersaoOriginal(), original.getVersao()))
            throw new org.springframework.dao.OptimisticLockingFailureException("Operacao " + original.getId());
        if (!original.isConfirmada() || original instanceof Estorno || possuiOutroEstorno(original.getId(), f.getId()))
            throw erro("estorno.operacao.invalida");
        if (f.getData().isBefore(original.getData())) throw erro("estorno.data.invalida");
        var builder = Estorno.builder();
        preencherDadosComuns(builder, f);
        return builder.operacaoOriginal(original)
            .programa(original.getPrograma())
            .quantidade(original.getQuantidade())
            .valor(original.getValor())
            .build();
    }

    private boolean possuiOutroEstorno(Long originalId, Long ignorarId) {
        return operacoes.findAll().stream().anyMatch(o -> o instanceof Estorno e && e.isConfirmada()
            && !Objects.equals(e.getId(), ignorarId)
            && Objects.equals(e.getOperacaoOriginal().getId(), originalId));
    }

    private Operacao carregarParaAlterar(Long id, Long versao) {
        Operacao operacao = operacoes.findById(id).orElseThrow(() -> erro("operacao.nao.encontrada"));
        if (!Objects.equals(versao, operacao.getVersao()))
            throw new org.springframework.dao.OptimisticLockingFailureException("Operacao " + id);
        return operacao;
    }

    private void verificarEstornosVinculados(Long id) {
        if (operacoes.findAll().stream().anyMatch(o -> o instanceof Estorno e
            && Objects.equals(e.getOperacaoOriginal().getId(), id)))
            throw erro("operacao.estorno.vinculado");
    }

    public void excluirOperacao(Long id, Long versao) {
        programas.bloquearTodos();
        Operacao operacao = carregarParaAlterar(id, versao);
        verificarEstornosVinculados(id);
        List<Operacao> creditos = operacoes.findAll().stream()
            .filter(o -> o.isCreditoTransferencia() && Objects.equals(o.getTransferenciaOrigem().getId(), id)).toList();
        creditos.forEach(o -> verificarEstornosVinculados(o.getId()));
        operacoes.deleteAll(creditos);
        operacoes.flush();
        operacoes.delete(operacao);
        operacoes.flush();
        atualizarConsolidados();
    }

    public void cancelarOperacao(Long id, Long versao) {
        alterarStatus(id, versao, StatusOperacao.CANCELADO);
    }

    public void confirmarAcumulo(Long id, Long versao) {
        programas.bloquearTodos();
        if (!(carregarParaAlterar(id, versao) instanceof Acumulo)) throw erro("operacao.tipo.invalido");
        alterarStatus(id, versao, StatusOperacao.CONFIRMADO);
    }

    private Transferencia editarTransferencia(OperacaoFormulario formulario, Long programaId, Long destinoId) {
        iniciarOperacao(formulario);
        Operacao registro = carregarParaAlterar(formulario.getId(), formulario.getVersao());
        if (!(registro instanceof Transferencia transferencia)) throw erro("operacao.tipo.invalido");
        verificarEstornosVinculados(registro.getId());
        ProgramaFidelidade origem = programa(programaId);
        ProgramaFidelidade destino = programa(destinoId);
        ativo(origem);
        ativo(destino);
        if (Objects.equals(programaId, destinoId)) throw erro("interface.origem.e.destino.devem.ser.diferentes");
        Calculos.naoNegativo(formulario.getQuantidade(), Textos.get("campo.quantidade"));
        Calculos.naoNegativo(formulario.getValor(), Textos.get("campo.valor"));
        Calculos.naoNegativo(formulario.getValorAdicional(), Textos.get("campo.valor"));
        validarPrecisao(formulario.getQuantidade());
        validarPrecisao(formulario.getValor());
        validarPrecisao(formulario.getValorAdicional());
        if (formulario.getValorAdicional().compareTo(formulario.getValor()) > 0)
            throw erro("operacao.adicional.invalido");
        transferencia.setPrograma(origem);
        transferencia.setDestino(destino);
        transferencia.setData(formulario.getData());
        transferencia.setQuantidade(formulario.getQuantidade());
        transferencia.setValor(formulario.getValor());
        transferencia.setValorAdicional(formulario.getValorAdicional());
        transferencia.setObservacoes(formulario.getObservacoes());
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

    private boolean possuiEstorno(Long id) {
        return operacoes.findAll().stream().anyMatch(o -> o instanceof Estorno e && e.isConfirmada()
            && Objects.equals(e.getOperacaoOriginal().getId(), id));
    }

    private void validarPrecisao(BigDecimal valor) {
        if (valor == null || valor.stripTrailingZeros().scale() > 2) throw erro("operacao.precisao.invalida");
    }

    private IllegalArgumentException erro(String chave) {
        return new IllegalArgumentException(Textos.get(chave));
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

    /** Participa da transação da importação; a limpeza é revertida se houver falha. */
    public void limparHistoricoParaImportacao() {
        programas.bloquearTodos();
        operacoes.excluirEstornos();
        operacoes.excluirAcumulos();
        operacoes.excluirHistorico();
        atualizarConsolidados();
    }

    public void confirmarTransferenciaImportada(Long id) {
        programas.bloquearTodos();
        for (Operacao o : operacoes.findAllByOrderByDataAscIdAsc()) {
            if (Objects.equals(o.getId(), id) || (o.isCreditoTransferencia()
                && Objects.equals(o.getTransferenciaOrigem().getId(), id)))
                o.setStatus(StatusOperacao.CONFIRMADO);
        }
        operacoes.flush();
        atualizarConsolidados();
    }

    private void criarCredito(Transferencia origem, ParcelaTransferencia parcela,
        BigDecimal pontos, BigDecimal custo) {
        Acumulo credito = Acumulo.builder()
            .programa(origem.getDestino())
            .data(origem.getData())
            .quantidade(pontos)
            .valor(Dinheiro.de(custo))
            .status(origem.getStatus())
            .transferenciaOrigem(origem)
            .parcelaTransferencia(parcela)
            .build();
        acumulos.saveAndFlush(credito);
    }

    /** Histórico e posição consolidada são gravados na mesma transação. */
    public void atualizarConsolidados() {
        programas.bloquearTodos();
        reconstruir().values().forEach(r -> {
            Consolidado c = consolidados.findById(r.getPrograma().getId()).orElseGet(() -> Consolidado.builder().programa(r.getPrograma()).build());
            c.setPrograma(r.getPrograma());
            c.setAcumulado(r.getAcumulado());
            c.setSaldo(r.getSaldo());
            c.setMilheiro(r.getMilheiro());
            consolidados.save(c);
        });
        consolidados.flush();
    }

    @Transactional(readOnly = true)
    public List<Consolidado> consolidados() { return consolidados.findAll(); }

    @Transactional(readOnly = true)
    public List<Resumo> resumos() { return new ArrayList<>(reconstruir().values()); }

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
        tratamentos.put(TipoOperacao.ACUMULO, this::consolidarAcumulo);
        tratamentos.put(TipoOperacao.VENDA, this::consolidarVenda);
        tratamentos.put(TipoOperacao.RESGATE, this::consolidarSaida);
        tratamentos.put(TipoOperacao.TRANSFERENCIA, this::consolidarSaida);
        tratamentos.put(TipoOperacao.ESTORNO, (operacao, resumo) -> consolidarEstorno((Estorno) operacao, resumo, estornadas));
        for (Operacao operacao : ordenado) {
            if (!operacao.isConfirmada()) continue;
            tratamentos.get(operacao.getTipo()).accept(operacao, mapa.get(operacao.getPrograma().getId()));
        }
        return mapa;
    }

    private void consolidarAcumulo(Operacao operacao, Resumo resumo) {
        resumo.entrada(operacao.getQuantidade(), operacao.getValor());
    }

    private void consolidarVenda(Operacao operacao, Resumo resumo) {
        consolidarSaida(operacao, resumo);
        resumo.setVendas(resumo.getVendas().add(operacao.getValor()));
    }

    private void consolidarSaida(Operacao operacao, Resumo resumo) {
        resumo.saida(operacao.getQuantidade());
    }

    private void consolidarEstorno(Estorno estorno, Resumo resumo, Set<Long> estornadas) {
        Operacao original = estorno.getOperacaoOriginal();
        if (!original.isConfirmada() || original instanceof Estorno
            || !estornadas.add(original.getId()) || estorno.getData().isBefore(original.getData()))
            throw erro("estorno.operacao.invalida");
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

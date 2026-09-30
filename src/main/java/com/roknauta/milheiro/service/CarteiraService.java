package com.roknauta.milheiro.service;

import com.roknauta.milheiro.domain.*;
import com.roknauta.milheiro.dto.*;
import com.roknauta.milheiro.helper.TransferenciaHelper;
import com.roknauta.milheiro.repository.ConsolidadoRepository;
import com.roknauta.milheiro.web.Textos;
import com.roknauta.milheiro.repository.FatorRepository;
import com.roknauta.milheiro.repository.OperacaoRepository;
import com.roknauta.milheiro.repository.ProgramaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Transactional
public class CarteiraService {

    private final ProgramaRepository programas;
    private final FatorRepository fatores;
    private final OperacaoRepository operacoes;
    private final ConsolidadoRepository consolidados;

    public CarteiraService(ProgramaRepository programas, FatorRepository fatores, OperacaoRepository operacoes, ConsolidadoRepository consolidados) {
        this.programas = programas;
        this.fatores = fatores;
        this.operacoes = operacoes;
        this.consolidados = consolidados;
    }

    @Transactional(readOnly = true)
    public List<Programa> programas() {
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

    public Programa salvarPrograma(Programa p) {
        programas.bloquearTodos();
        if (p.getNome() == null || p.getNome().trim().isEmpty() || p.getNome().trim().length() > 80)
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.informe.um.nome.de.ate.80.caracteres"));
        p.setNome(p.getNome().trim());
        if (programas.existsByNomeIgnoreCaseAndIdNot(p.getNome(), p.getId() == null ? -1L : p.getId()))
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.ja.existe.um.programa.com.esse.nome"));
        if (p.getCategoria() == null)
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.categoria.invalida"));
        Programa salvo = programas.saveAndFlush(p);
        atualizarConsolidados();
        return salvo;
    }

    public void excluirPrograma(Long id) {
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

    private Programa programa(Long id) {
        if (id == null)
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.selecione.o.programa.2"));
        return programas.findById(id).orElseThrow(() -> new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.programa.nao.encontrado")));
    }

    private void ativo(Programa p) {
        if (!p.isAtivo())
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("interface.o.programa") + " " + p.getNome() + " " + com.roknauta.milheiro.web.Textos.get("interface.esta.inativo"));
    }

    public Operacao salvarOperacao(OperacaoFormulario f, Long programaId, Long destinoId) {
        programas.bloquearTodos();
        if (f.getData() == null || f.getTipo() == null) throw erro("interface.informe.data.e.tipo");
        if (f.getObservacoes() != null && f.getObservacoes().length() > 500)
            throw erro("interface.observacoes.maximo.de.500.caracteres");
        Operacao o;
        if (f.isEstorno()) {
            o = prepararEstorno(f);
        } else {
            o = switch (f.getTipo()) {
                case ACUMULO -> new Acumulo();
                case VENDA -> new Venda();
                case RESGATE -> new Resgate();
                case TRANSFERENCIA -> new Transferencia();
                default -> throw erro("interface.informe.data.e.tipo");
            };
            o.setPrograma(programa(programaId));
            ativo(o.getPrograma());
            Calculos.positivo(f.getQuantidade(), Textos.get("campo.quantidade"));
            validarPrecisao(f.getQuantidade());
            Calculos.naoNegativo(f.getValor(), Textos.get("campo.valor"));
            Calculos.naoNegativo(f.getTaxas(), Textos.get("campo.taxas"));
            validarPrecisao(f.getValor());
            validarPrecisao(f.getTaxas());
            o.setQuantidade(f.getQuantidade());
            o.setValor(f.getTipo() == TipoOperacao.RESGATE ? BigDecimal.ZERO : f.getValor());
            o.setDesembolso(f.getTaxas());
            if (o instanceof Acumulo) {
                o.setValor(o.getValor().add(f.getTaxas()));
                o.setDesembolso(o.getValor());
            }
        }
        o.setData(f.getData());
        o.setObservacoes(f.getObservacoes());
        o.setChaveImportacao(f.getChaveImportacao());
        if (o instanceof Transferencia transferencia) {
            transferencia.setDestino(programa(destinoId));
            ativo(transferencia.getDestino());
            if (Objects.equals(programaId, destinoId))
                throw erro("interface.origem.e.destino.devem.ser.diferentes");
            if (f.isComCarrinho()) {
                validarPrecisao(f.getPontosDebitarSaldo());
                validarPrecisao(f.getValorCarrinho());
            }
            ResultadoTransferencia resultado = TransferenciaHelper.calcular(f.parametros(),
                resumoAntesDaOperacao(f.getData(), programaId), false, 2, RoundingMode.DOWN);
            if (resultado.creditos().signum() <= 0)
                throw erro("interface.a.transferencia.precisa.gerar.pelo.menos.0.01.ponto");
            transferencia.setQuantidade(resultado.debito());
            transferencia.setValor(resultado.custoTotal().setScale(2, RoundingMode.HALF_UP));
            transferencia.setDesembolso(resultado.desembolso());
            transferencia.setStatus(StatusOperacao.PENDENTE);
            operacoes.saveAndFlush(transferencia);
            criarCredito(transferencia, ParcelaTransferencia.BASE, resultado.base(), transferencia.getValor());
            if (resultado.bonus().signum() > 0)
                criarCredito(transferencia, ParcelaTransferencia.BONUS, resultado.bonus(), BigDecimal.ZERO);
        } else {
            operacoes.saveAndFlush(o);
        }
        atualizarConsolidados();
        return o;
    }

    private Estorno prepararEstorno(OperacaoFormulario f) {
        if (f.getOperacaoOriginalId() == null) throw erro("estorno.selecione.operacao");
        Operacao original = operacoes.findById(f.getOperacaoOriginalId())
            .orElseThrow(() -> erro("estorno.selecione.operacao"));
        if (!Objects.equals(f.getVersaoOriginal(), original.getVersao()))
            throw new org.springframework.dao.OptimisticLockingFailureException("Operacao " + original.getId());
        if (!original.isConfirmada() || original instanceof Estorno || possuiEstorno(original.getId()))
            throw erro("estorno.operacao.invalida");
        if (f.getData().isBefore(original.getData())) throw erro("estorno.data.invalida");
        Estorno estorno = new Estorno();
        estorno.setOperacaoOriginal(original);
        estorno.setPrograma(original.getPrograma());
        estorno.setQuantidade(original.getQuantidade());
        estorno.setValor(original.getValor());
        estorno.setDesembolso(original.getDesembolso());
        return estorno;
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
        Acumulo credito = new Acumulo();
        credito.setPrograma(origem.getDestino());
        credito.setData(origem.getData());
        credito.setQuantidade(pontos);
        credito.setValor(custo);
        credito.setStatus(origem.getStatus());
        credito.setTransferenciaOrigem(origem);
        credito.setParcelaTransferencia(parcela);
        operacoes.saveAndFlush(credito);
    }

    /** Histórico e posição consolidada são gravados na mesma transação. */
    public void atualizarConsolidados() {
        programas.bloquearTodos();
        reconstruir().values().forEach(r -> {
            Consolidado c = consolidados.findById(r.getPrograma().getId()).orElseGet(Consolidado::new);
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
        for (Operacao o : ordenado) {
            if (!o.isConfirmada()) continue;
            Resumo r = mapa.get(o.getPrograma().getId());
            switch (o.getTipo()) {
                case ACUMULO -> r.entrada(o.getQuantidade(), o.getValor());
                case VENDA -> {
                    r.saida(o.getQuantidade());
                    r.setVendas(r.getVendas().add(o.getValor()).subtract(o.getDesembolso()));
                }
                case RESGATE, TRANSFERENCIA -> r.saida(o.getQuantidade());
                case ESTORNO -> {
                    Estorno e = (Estorno) o;
                    Operacao original = e.getOperacaoOriginal();
                    if (!original.isConfirmada() || original instanceof Estorno
                        || !estornadas.add(original.getId()) || e.getData().isBefore(original.getData()))
                        throw erro("estorno.operacao.invalida");
                    if (original instanceof Acumulo) {
                        r.saida(e.getQuantidade());
                        r.setAcumulado(r.getAcumulado().subtract(e.getQuantidade()));
                        r.setGasto(r.getGasto().subtract(e.getValor()));
                    } else {
                        r.setSaldo(r.getSaldo().add(e.getQuantidade()));
                        if (original instanceof Venda)
                            r.setVendas(r.getVendas().subtract(e.getValor()).add(e.getDesembolso()));
                    }
                }
            }
        }
        return mapa;
    }
}

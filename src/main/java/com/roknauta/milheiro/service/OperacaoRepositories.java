package com.roknauta.milheiro.service;

import com.roknauta.milheiro.repository.ProgramaFidelidadeRepository;
import com.roknauta.milheiro.repository.OperacaoRepository;
import com.roknauta.milheiro.repository.ConsolidadoRepository;
import org.springframework.stereotype.Component;

/** Somente dependências comuns à hierarquia dos serviços de operações. */
@Component
public record OperacaoRepositories(
        ProgramaFidelidadeRepository programas,
        OperacaoRepository operacoes,
        ConsolidadoRepository consolidados,
        com.roknauta.milheiro.service.mapper.ConsolidadoMapper consolidadoMapper) { }

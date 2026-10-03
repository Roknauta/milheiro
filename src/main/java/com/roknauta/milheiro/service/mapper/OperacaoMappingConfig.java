package com.roknauta.milheiro.service.mapper;

import com.roknauta.milheiro.domain.Operacao;
import com.roknauta.milheiro.dto.crud.OperacaoDTO;
import org.mapstruct.*;

/** Configuração compartilhada, sem implementação de conversões por entidade. */
@MapperConfig
public interface OperacaoMappingConfig {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "versao", source = "versao")
    @Mapping(target = "tipo", source = "tipo")
    @Mapping(target = "data", source = "data")
    @Mapping(target = "quantidade", source = "quantidade")
    @Mapping(target = "valor", source = "valor")
    @Mapping(target = "observacoes", source = "observacoes")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "programa", source = "programa")
    @Mapping(target = "programaId", source = "programa.id")
    OperacaoDTO toDTO(Operacao entity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "data", source = "data")
    @Mapping(target = "quantidade", source = "quantidade")
    @Mapping(target = "valor", source = "valor")
    @Mapping(target = "observacoes", source = "observacoes")
    void atualizarAtributosComuns(OperacaoDTO dto, @MappingTarget Operacao entity);
}

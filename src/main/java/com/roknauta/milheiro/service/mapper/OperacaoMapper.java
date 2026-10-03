package com.roknauta.milheiro.service.mapper;

import com.roknauta.milheiro.domain.Operacao;
import com.roknauta.milheiro.dto.crud.OperacaoDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", config = OperacaoMappingConfig.class, uses = ProgramaFidelidadeMapper.class)
public interface OperacaoMapper {
    @InheritConfiguration(name = "toDTO")
    OperacaoDTO toDTO(Operacao entity);
}

package com.roknauta.milheiro.service.mapper;

import com.roknauta.milheiro.domain.Estorno;
import com.roknauta.milheiro.dto.crud.EstornoDTO;
import org.mapstruct.*;

@Mapper(builder = @Builder(disableBuilder = true), componentModel = "spring", config = OperacaoMappingConfig.class, uses = {ProgramaFidelidadeMapper.class, OperacaoMapper.class})
public interface EstornoMapper {
    @InheritConfiguration(name = "toDTO")
    @Mapping(target = "operacaoOriginal", source = "operacaoOriginal")
    @Mapping(target = "operacaoOriginalId", source = "operacaoOriginal.id")
    @Mapping(target = "versaoOriginal", source = "operacaoOriginal.versao")
    EstornoDTO toDTO(Estorno entity);

    @InheritConfiguration(name = "atualizarAtributosComuns")
    Estorno toEntity(EstornoDTO dto);

    @InheritConfiguration(name = "atualizarAtributosComuns")
    void updateEntity(EstornoDTO dto, @MappingTarget Estorno entity);
}

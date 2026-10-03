package com.roknauta.milheiro.service.mapper;

import com.roknauta.milheiro.domain.FatorConversao;
import com.roknauta.milheiro.dto.crud.FatorConversaoDTO;
import org.mapstruct.*;

@Mapper(builder = @Builder(disableBuilder = true), componentModel = "spring", uses = ProgramaFidelidadeMapper.class)
public interface FatorConversaoMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "origem", source = "origem")
    @Mapping(target = "destino", source = "destino")
    @Mapping(target = "origemId", source = "origem.id")
    @Mapping(target = "destinoId", source = "destino.id")
    @Mapping(target = "pontosOrigem", source = "pontosOrigem")
    @Mapping(target = "pontosDestino", source = "pontosDestino")
    FatorConversaoDTO toDTO(FatorConversao entity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "pontosOrigem", source = "pontosOrigem")
    @Mapping(target = "pontosDestino", source = "pontosDestino")
    FatorConversao toEntity(FatorConversaoDTO dto);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(FatorConversaoDTO dto, @MappingTarget FatorConversao entity);
}

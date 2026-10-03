package com.roknauta.milheiro.service.mapper;

import com.roknauta.milheiro.domain.Resgate;
import com.roknauta.milheiro.dto.crud.ResgateDTO;
import org.mapstruct.*;

@Mapper(builder = @Builder(disableBuilder = true), componentModel = "spring", config = OperacaoMappingConfig.class, uses = ProgramaFidelidadeMapper.class)
public interface ResgateMapper {
    @InheritConfiguration(name = "toDTO")
    @Mapping(target = "taxas", source = "valor")
    ResgateDTO toDTO(Resgate entity);

    @InheritConfiguration(name = "atualizarAtributosComuns")
    Resgate toEntity(ResgateDTO dto);

    @InheritConfiguration(name = "atualizarAtributosComuns")
    void updateEntity(ResgateDTO dto, @MappingTarget Resgate entity);
}

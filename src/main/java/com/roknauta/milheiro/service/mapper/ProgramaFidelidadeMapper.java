package com.roknauta.milheiro.service.mapper;

import com.roknauta.milheiro.domain.ProgramaFidelidade;
import com.roknauta.milheiro.dto.crud.ProgramaFidelidadeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Builder;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(builder = @Builder(disableBuilder = true), componentModel = "spring")
public interface ProgramaFidelidadeMapper {

    ProgramaFidelidadeDTO toDTO(ProgramaFidelidade entity);

    @Mapping(target = "id", ignore = true)
    ProgramaFidelidade toEntity(ProgramaFidelidadeDTO dto);

    @Mapping(target = "id", ignore = true)
    void updateEntity(
            ProgramaFidelidadeDTO dto,
            @MappingTarget ProgramaFidelidade entity
    );
}

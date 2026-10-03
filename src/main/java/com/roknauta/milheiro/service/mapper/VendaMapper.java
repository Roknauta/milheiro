package com.roknauta.milheiro.service.mapper;

import com.roknauta.milheiro.domain.Venda;
import com.roknauta.milheiro.dto.crud.VendaDTO;
import org.mapstruct.*;

@Mapper(builder = @Builder(disableBuilder = true), componentModel = "spring", config = OperacaoMappingConfig.class, uses = ProgramaFidelidadeMapper.class)
public interface VendaMapper {
    @InheritConfiguration(name = "toDTO")
    VendaDTO toDTO(Venda entity);

    @InheritConfiguration(name = "atualizarAtributosComuns")
    Venda toEntity(VendaDTO dto);

    @InheritConfiguration(name = "atualizarAtributosComuns")
    void updateEntity(VendaDTO dto, @MappingTarget Venda entity);
}

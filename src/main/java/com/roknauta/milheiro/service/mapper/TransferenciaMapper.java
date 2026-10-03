package com.roknauta.milheiro.service.mapper;

import com.roknauta.milheiro.domain.Transferencia;
import com.roknauta.milheiro.dto.crud.TransferenciaDTO;
import org.mapstruct.*;

@Mapper(builder = @Builder(disableBuilder = true), componentModel = "spring", config = OperacaoMappingConfig.class, uses = ProgramaFidelidadeMapper.class)
public interface TransferenciaMapper {
    @InheritConfiguration(name = "toDTO")
    @Mapping(target = "destino", source = "destino")
    @Mapping(target = "destinoId", source = "destino.id")
    @Mapping(target = "valorAdicional", source = "valorAdicional")
    TransferenciaDTO toDTO(Transferencia entity);

    @InheritConfiguration(name = "atualizarAtributosComuns")
    @Mapping(target = "valorAdicional", source = "valorAdicional")
    Transferencia toEntity(TransferenciaDTO dto);

    @InheritConfiguration(name = "atualizarAtributosComuns")
    @Mapping(target = "valorAdicional", source = "valorAdicional")
    void updateEntity(TransferenciaDTO dto, @MappingTarget Transferencia entity);
}

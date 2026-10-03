package com.roknauta.milheiro.service.mapper;

import com.roknauta.milheiro.domain.Consolidado;
import com.roknauta.milheiro.service.Resumo;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ConsolidadoMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "programa", source = "programa")
    @Mapping(target = "acumulado", source = "acumulado")
    @Mapping(target = "saldo", source = "saldo")
    @Mapping(target = "milheiro", source = "milheiro")
    Consolidado toEntity(Resumo resumo);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(Resumo resumo, @MappingTarget Consolidado entity);
}

package com.roknauta.milheiro.service.mapper;

import com.roknauta.milheiro.domain.Acumulo;
import com.roknauta.milheiro.dto.crud.AcumuloDTO;
import org.mapstruct.*;

@Mapper(builder = @Builder(disableBuilder = true), componentModel = "spring", config = OperacaoMappingConfig.class, uses = ProgramaFidelidadeMapper.class)
public interface AcumuloMapper {
    @InheritConfiguration(name = "toDTO")
    @Mapping(target = "vinculoTransferencia", source = "vinculoTransferencia")
    @Mapping(target = "parcelaDescricao", source = "parcelaDescricao")
    AcumuloDTO toDTO(Acumulo entity);

    @InheritConfiguration(name = "atualizarAtributosComuns")
    Acumulo toEntity(AcumuloDTO dto);

    @InheritConfiguration(name = "atualizarAtributosComuns")
    void updateEntity(AcumuloDTO dto, @MappingTarget Acumulo entity);
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "programa", source = "origem.destino")
    @Mapping(target = "data", source = "origem.data")
    @Mapping(target = "quantidade", source = "pontos")
    @Mapping(target = "valor", source = "custo")
    @Mapping(target = "status", source = "origem.status")
    @Mapping(target = "transferenciaOrigem", source = "origem")
    @Mapping(target = "parcelaTransferencia", source = "parcela")
    Acumulo toCredito(com.roknauta.milheiro.domain.Transferencia origem,
                     com.roknauta.milheiro.domain.ParcelaTransferencia parcela,
                     java.math.BigDecimal pontos, com.roknauta.milheiro.domain.Dinheiro custo);
}

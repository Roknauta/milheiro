package com.roknauta.milheiro.persistence;

import com.roknauta.milheiro.domain.Dinheiro;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.math.BigDecimal;

@Converter
public class DinheiroPersistenceConverter implements AttributeConverter<Dinheiro, BigDecimal> {
    @Override
    public BigDecimal convertToDatabaseColumn(Dinheiro valor) {
        return valor == null ? null : new BigDecimal(valor.unscaledValue(), valor.scale());
    }

    @Override
    public Dinheiro convertToEntityAttribute(BigDecimal valor) {
        return Dinheiro.de(valor);
    }
}

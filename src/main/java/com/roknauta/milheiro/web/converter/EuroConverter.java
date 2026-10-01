package com.roknauta.milheiro.web.converter;

import jakarta.faces.convert.FacesConverter;

@FacesConverter("euroConverter")
public class EuroConverter extends MonetarioConverter {
    @Override
    protected String codigoMoeda() { return "EUR"; }

    public EuroConverter() { setCurrencySymbol("€"); }
}

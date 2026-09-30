package com.roknauta.milheiro.web.converter;

import jakarta.faces.convert.FacesConverter;

@FacesConverter("euroConverter")
public class EuroConverter extends MonetarioConverter {
    public EuroConverter() { setCurrencySymbol("€"); }
}

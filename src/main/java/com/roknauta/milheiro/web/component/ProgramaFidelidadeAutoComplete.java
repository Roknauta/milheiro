package com.roknauta.milheiro.web.component;

import com.roknauta.milheiro.domain.ProgramaFidelidade;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UINamingContainer;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@FacesComponent("programaFidelidadeAutoComplete")
public class ProgramaFidelidadeAutoComplete extends UINamingContainer {
    @SuppressWarnings("unchecked")
    private List<ProgramaFidelidade> programas() {
        Object valor = getAttributes().get("programas");
        return valor == null ? List.of() : (List<ProgramaFidelidade>) valor;
    }

    public List<Long> completar(String consulta) {
        String filtro = normalizar(consulta);
        return programas().stream().filter(ProgramaFidelidade::isAtivo)
            .filter(p -> normalizar(p.getNome()).contains(filtro))
            .map(ProgramaFidelidade::getId).toList();
    }

    public String nome(Long id) {
        if (id == null) return "";
        return programas().stream().filter(p -> Objects.equals(p.getId(), id))
            .map(ProgramaFidelidade::getNome).findFirst().orElse("");
    }

    private String normalizar(String texto) {
        return Normalizer.normalize(texto == null ? "" : texto.trim(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}

package com.roknauta.milheiro.web.crud;

import com.roknauta.milheiro.service.crud.ProgramaFidelidadeService;
import jakarta.faces.model.SelectItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CrudHelper {
    private final ProgramaFidelidadeService programaFidelidadeService;

    public List<SelectItem> getProgramasFidelidade(String nome) {
        return programaFidelidadeService.buscarPorNome(nome).stream()
                .map(dto -> new SelectItem(dto.getId(), dto.getNome())).toList();
    }
}

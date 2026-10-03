package com.roknauta.milheiro.dto.crud;

import com.roknauta.milheiro.domain.CategoriaProgramaFidelidade;
import com.roknauta.milheiro.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgramaFidelidadeDTO extends BaseDTO {

    private String nome;
    private CategoriaProgramaFidelidade categoria;
    private boolean ativo = true;
}

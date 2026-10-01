package com.roknauta.milheiro.domain;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "programa")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ProgramaFidelidade extends EntidadeBase {

    @Column(nullable = false, unique = true, length = 80)
    private String nome;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoriaProgramaFidelidade categoria;
    @Builder.Default
    private boolean ativo = true;

    public boolean isAll() {
        return nome != null && java.util.regex.Pattern.compile("(?i)\\b(all|accor)\\b").matcher(nome).find();
    }
}

package com.roknauta.milheiro.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Programa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String nome;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoriaPrograma categoria;
    private boolean ativo = true;

    public boolean isAll() {
        return nome != null && java.util.regex.Pattern.compile("(?i)\\b(all|accor)\\b").matcher(nome).find();
    }
}

package com.roknauta.milheiro.repository;

import com.roknauta.milheiro.domain.Programa;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;

import java.util.List;

public interface ProgramaRepository extends JpaRepository<Programa, Long> {

    List<Programa> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Programa p order by p.id")
    List<Programa> bloquearTodos();
}

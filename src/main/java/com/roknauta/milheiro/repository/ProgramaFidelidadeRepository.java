package com.roknauta.milheiro.repository;

import com.roknauta.milheiro.domain.ProgramaFidelidade;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;

import java.util.List;

public interface ProgramaFidelidadeRepository extends JpaRepository<ProgramaFidelidade, Long> {

    List<ProgramaFidelidade> findAllByOrderByNomeAsc();

    List<ProgramaFidelidade> findByAtivoTrueAndNomeContainingIgnoreCaseOrderByNomeAsc(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProgramaFidelidade p order by p.id")
    List<ProgramaFidelidade> bloquearTodos();
}

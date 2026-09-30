package com.roknauta.milheiro.repository;

import com.roknauta.milheiro.domain.Operacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperacaoRepository extends JpaRepository<Operacao, Long> {

    boolean existsByChaveImportacao(String chaveImportacao);

    List<Operacao> findAllByOrderByDataAscIdAsc();


}

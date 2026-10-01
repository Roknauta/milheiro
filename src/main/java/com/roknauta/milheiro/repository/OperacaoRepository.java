package com.roknauta.milheiro.repository;

import com.roknauta.milheiro.domain.Operacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OperacaoRepository extends JpaRepository<Operacao, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Estorno")
    void excluirEstornos();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Acumulo")
    void excluirAcumulos();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Operacao")
    void excluirHistorico();

    List<Operacao> findAllByOrderByDataAscIdAsc();


}

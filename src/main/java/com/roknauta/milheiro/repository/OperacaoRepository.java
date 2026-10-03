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

    @Query("select count(e) > 0 from Estorno e where e.operacaoOriginal.id = :id")
    boolean possuiEstornoVinculado(@org.springframework.data.repository.query.Param("id") Long id);

    @Query("select count(e) > 0 from Estorno e where e.operacaoOriginal.id = :id and e.status = :status")
    boolean possuiEstornoConfirmado(@org.springframework.data.repository.query.Param("id") Long id,
                                  @org.springframework.data.repository.query.Param("status") com.roknauta.milheiro.domain.StatusOperacao status);


}

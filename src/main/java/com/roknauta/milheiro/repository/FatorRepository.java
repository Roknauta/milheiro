package com.roknauta.milheiro.repository;

import com.roknauta.milheiro.domain.FatorConversao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FatorRepository extends JpaRepository<FatorConversao, Long> {

    Optional<FatorConversao> findByOrigemIdAndDestinoId(Long origem, Long destino);

    boolean existsByOrigemIdOrDestinoId(Long origem, Long destino);
}

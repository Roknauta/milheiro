package com.roknauta.milheiro.config;

import com.roknauta.milheiro.service.CarteiraService;
import com.roknauta.milheiro.service.MigracaoOperacoes;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class HistoricoOperacoesConfig {
    @Bean
    ApplicationRunner atualizarHistoricoOperacoes(MigracaoOperacoes migracao, CarteiraService service) {
        return args -> {
            migracao.normalizar();
            service.atualizarConsolidados();
            migracao.concluir();
        };
    }
}

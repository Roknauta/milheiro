package com.roknauta.milheiro.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "milheiro.hsql.enabled", havingValue = "true")
@EnableConfigurationProperties(DataSourceProperties.class)
public class HsqlServerConfig {

    @Bean(initMethod = "start", destroyMethod = "close")
    public HsqlDatabaseServer hsqlServer(
            @Value("${milheiro.hsql.database-path}") String path,
            @Value("${milheiro.hsql.port}") int port,
            @Value("${milheiro.hsql.database-name}") String name,
            DataSourceProperties properties) {
        var server = new HsqlDatabaseServer(Path.of(path), port, name);
        // Impede iniciar um catálogo enquanto o datasource aponta para outro servidor.
        String expected = "jdbc:hsqldb:hsql://127.0.0.1:" + port + "/" + name;
        String configured = properties.getUrl();
        if (configured == null || !configured.replace("//localhost:", "//127.0.0.1:").equals(expected)) {
            throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.formatar("banco.endereco.incompativel", expected));
        }
        return server;
    }

    @Bean(destroyMethod = "close")
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(HsqlDatabaseServer hsqlServer, DataSourceProperties properties) {
        // A dependência garante início do banco antes do pool e fechamento do pool antes do banco.
        return properties.initializeDataSourceBuilder()
            .type(HikariDataSource.class).url(hsqlServer.getJdbcUrl()).build();
    }
}

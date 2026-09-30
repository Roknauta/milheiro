package com.roknauta.milheiro.config;

import org.hsqldb.Database;
import org.hsqldb.Server;
import org.hsqldb.server.ServerConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Ciclo de vida do catálogo em arquivo pertencente à aplicação. */
public final class HsqlDatabaseServer implements AutoCloseable {
    private final Server server = new Server();
    private final Path path;
    private final String name;

    public HsqlDatabaseServer(Path path, int port, String name) {
        if (port < 0 || port > 65535) throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("banco.porta.invalida"));
        if (!name.matches("[a-zA-Z0-9_-]+")) throw new IllegalArgumentException(com.roknauta.milheiro.web.Textos.get("banco.nome.invalido"));
        this.path = path.toAbsolutePath().normalize();
        this.name = name;
        server.setNoSystemExit(true);
        server.setAddress("127.0.0.1");
        server.setPort(port);
        server.setDatabaseName(0, name);
        server.setDatabasePath(0, "file:" + this.path);
        server.setTrace(false);
        server.setSilent(true);
    }

    public void start() {
        try {
            Files.createDirectories(path.getParent());
            server.start();
            if (server.getState() != ServerConstants.SERVER_STATE_ONLINE) {
                throw new IllegalStateException(com.roknauta.milheiro.web.Textos.get("banco.inicio.falhou"), server.getServerError());
            }
        } catch (IOException | RuntimeException e) {
            close();
            throw new IllegalStateException(com.roknauta.milheiro.web.Textos.formatar("banco.inicio.caminho", path), e);
        }
    }

    public String getJdbcUrl() {
        return "jdbc:hsqldb:hsql://127.0.0.1:" + server.getLocalPort() + "/" + name;
    }

    @Override
    public void close() {
        // Fecha apenas os catálogos deste servidor, gravando-os com SHUTDOWN normal.
        server.shutdownWithCatalogs(Database.CLOSEMODE_NORMAL);
    }
}

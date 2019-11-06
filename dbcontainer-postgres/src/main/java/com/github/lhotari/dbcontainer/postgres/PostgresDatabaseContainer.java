package com.github.lhotari.dbcontainer.postgres;

import com.github.lhotari.dbcontainer.DatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresDatabaseContainer implements DatabaseContainer {
    public static final String DEFAULT_POSTGRES_VERSION = "11-alpine";
    private String postgresVersion = System.getProperty("postgresVersion", DEFAULT_POSTGRES_VERSION);
    private final PostgreSQLContainer<?> postgresContainer;

    public PostgresDatabaseContainer() {
        this.postgresContainer = new PostgreSQLContainer<>("postgres:" + postgresVersion);
    }

    @Override
    public void start() {
        postgresContainer.start();
    }

    @Override
    public void stop() {
        postgresContainer.stop();
    }

    @Override
    public String getJdbcUrl() {
        return postgresContainer.getJdbcUrl();
    }

    @Override
    public String getR2dbcUrl() {
        return "r2dbc:postgresql://" + postgresContainer.getContainerIpAddress() + ":" + postgresContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT) + "/" + postgresContainer.getDatabaseName();
    }

    @Override
    public String getDatabaseUser() {
        return postgresContainer.getUsername();
    }

    @Override
    public String getDatabasePassword() {
        return postgresContainer.getPassword();
    }
}

package com.github.lhotari.dbcontainer;

public interface DatabaseContainer extends AutoCloseable {
    String getJdbcUrl();

    String getR2dbcUrl();

    String getDatabaseUser();

    String getDatabasePassword();

    void start();

    void stop();

    @Override
    default void close() {
        stop();
    }
}

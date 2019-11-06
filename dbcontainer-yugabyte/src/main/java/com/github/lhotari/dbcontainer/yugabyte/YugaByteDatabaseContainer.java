package com.github.lhotari.dbcontainer.yugabyte;

import com.github.lhotari.dbcontainer.DatabaseContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.concurrent.atomic.AtomicBoolean;

public class YugaByteDatabaseContainer implements DatabaseContainer {
    private static final String DEFAULT_YUGABYTE_VERSION = "latest";
    private static final int YCQL_SERVICE_PORT = 9042;
    private static final int YSQL_SERVICE_PORT = 5433;
    private static final String YSQL_DATABASE_NAME = "postgres";
    private static final String YSQL_DATABASE_USER = "postgres";
    private static final String YSQL_DATABASE_PASSWORD = "";
    private GenericContainer<?> ymasterContainer;
    private GenericContainer<?> tmasterContainer;
    private Network network;
    private final AtomicBoolean initialized = new AtomicBoolean();
    private String dockerImageName;

    public YugaByteDatabaseContainer() {
        withYugaByteVersion(System.getProperty("yugabyteVersion", DEFAULT_YUGABYTE_VERSION));
    }

    public YugaByteDatabaseContainer withYugaByteVersion(String dockerImageVersion) {
        this.dockerImageName = "yugabytedb/yugabyte:" + dockerImageVersion;
        return this;
    }

    @Override
    public synchronized void start() {
        if (initialized.compareAndSet(false, true)) {
            network = Network.newNetwork();
            ymasterContainer = new GenericContainer<>(dockerImageName)
                    .withCommand("/home/yugabyte/bin/yb-master",
                            "--fs_data_dirs=/mnt/disk0,/mnt/disk1",
                            "--master_addresses=yb-master-test:7100",
                            "--replication_factor=1",
                            "--enable_ysql=true",
                            "--callhome_enabled=false",
                            "--logtostderr")
                    .withExposedPorts(7100)
                    .withNetwork(network)
                    .withNetworkAliases("yb-master-test");
            tmasterContainer = new GenericContainer<>(dockerImageName)
                    .withCommand("/home/yugabyte/bin/yb-tserver",
                            "--fs_data_dirs=/mnt/disk0,/mnt/disk1",
                            "--start_pgsql_proxy",
                            "--pgsql_proxy_bind_address=yb-tserver-test:5433",
                            "--tserver_master_addrs=yb-master-test:7100",
                            "--callhome_enabled=false",
                            "--logtostderr")
                    .withExposedPorts(YSQL_SERVICE_PORT, YCQL_SERVICE_PORT, 9000)
                    .withNetwork(network)
                    .withNetworkAliases("yb-tserver-test")
                    .dependsOn(ymasterContainer);
            try {
                ymasterContainer.start();
                tmasterContainer.start();
            } catch (RuntimeException e) {
                ymasterContainer.stop();
                tmasterContainer.stop();
                network.close();
                throw e;
            }
        }
    }

    @Override
    public synchronized void stop() {
        if (initialized.compareAndSet(true, false)) {
            tmasterContainer.stop();
            tmasterContainer = null;
            ymasterContainer.stop();
            ymasterContainer = null;
            network.close();
            network = null;
        }
    }

    public String getYCQLHost() {
        return tmasterContainer.getContainerIpAddress();
    }

    public int getYCQLPort() {
        return tmasterContainer.getMappedPort(YCQL_SERVICE_PORT);
    }

    public String getYSQLHost() {
        return tmasterContainer.getContainerIpAddress();
    }

    public int getYSQLPort() {
        return tmasterContainer.getMappedPort(YSQL_SERVICE_PORT);
    }

    public String getJdbcUrl() {
        return "jdbc:postgresql://" + getYSQLHost() + ":" + getYSQLPort() + "/" + getDatabaseName();
    }

    public String getR2dbcUrl() {
        return "r2dbc:postgresql://" + getYSQLHost() + ":" + getYSQLPort() + "/" + getDatabaseName();
    }

    public String getDatabaseName() {
        return YSQL_DATABASE_NAME;
    }

    public String getDatabaseUser() {
        return YSQL_DATABASE_USER;
    }

    public String getDatabasePassword() {
        return YSQL_DATABASE_PASSWORD;
    }
}

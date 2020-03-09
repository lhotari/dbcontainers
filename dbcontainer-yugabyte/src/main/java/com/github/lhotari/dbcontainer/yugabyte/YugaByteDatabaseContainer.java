package com.github.lhotari.dbcontainer.yugabyte;

import com.github.dockerjava.api.model.HealthCheck;
import com.github.lhotari.dbcontainer.DatabaseContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class YugaByteDatabaseContainer implements DatabaseContainer {
    private static final String DEFAULT_YUGABYTE_VERSION = "latest";
    private static final int YCQL_SERVICE_PORT = 9042;
    private static final int YSQL_SERVICE_PORT = 5433;
    private static final String YSQL_DATABASE_NAME = "postgres";
    private static final String YSQL_DATABASE_USER = "postgres";
    private static final String YSQL_DATABASE_PASSWORD = "";
    private static final List<String> DEFAULT_MASTER_COMMAND = Collections.unmodifiableList(Arrays.asList("/home/yugabyte/bin/yb-master",
            "--fs_data_dirs=/mnt/disk0,/mnt/disk1",
            "--master_addresses=yb-master-test:7100",
            "--replication_factor=1",
            "--enable_ysql=true",
            "--callhome_enabled=false",
            "--logtostderr"));
    private static final List<String> DEFAULT_TSERVER_COMMAND = Collections.unmodifiableList(Arrays.asList("/home/yugabyte/bin/yb-tserver",
            "--fs_data_dirs=/mnt/disk0,/mnt/disk1",
            "--start_pgsql_proxy",
            "--pgsql_proxy_bind_address=yb-tserver-test:5433",
            "--tserver_master_addrs=yb-master-test:7100",
            "--callhome_enabled=false",
            "--logtostderr"));
    private static final String MASTER_NW_ALIAS = "yb-master-test";
    private static final String TSERVER_NW_ALIAS = "yb-tserver-test";
    private GenericContainer<?> ymasterContainer;
    private GenericContainer<?> tserverContainer;
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
                    .withCommand(customizeMasterCommand(new ArrayList<>(DEFAULT_MASTER_COMMAND)).toArray(new String[0]))
                    .withExposedPorts(7100)
                    .withNetwork(network)
                    .withNetworkAliases(MASTER_NW_ALIAS);
            tserverContainer = new GenericContainer<>(dockerImageName)
                    .withCommand(customizeTserverCommand(new ArrayList<>(DEFAULT_TSERVER_COMMAND)).toArray(new String[0]))
                    .withExposedPorts(YSQL_SERVICE_PORT, YCQL_SERVICE_PORT, 9000)
                    .withNetwork(network)
                    .withNetworkAliases(TSERVER_NW_ALIAS)
                    .dependsOn(ymasterContainer)
                    .withCreateContainerCmdModifier(createContainerCmd -> {
                        createContainerCmd.withHealthcheck(new HealthCheck()
                                .withTest(Arrays.asList("CMD-SHELL", String.format("/home/yugabyte/postgres/bin/pg_isready -h %s -U postgres -p 5433 -t 30 && /home/yugabyte/bin/cqlsh -e 'SELECT now() FROM system.local' --no-color --connect-timeout=30", TSERVER_NW_ALIAS)))
                                .withInterval(Duration.ofSeconds(10).toNanos())
                                .withTimeout(Duration.ofSeconds(60).toNanos())
                                .withRetries(5));
                    });
            customizeContainers(ymasterContainer, tserverContainer);
            try {
                ymasterContainer.start();
                tserverContainer.start();
            } catch (Throwable t) {
                ymasterContainer.stop();
                tserverContainer.stop();
                network.close();
                sneakyThrow(t);
            }
        }
    }

    // used to re-throw original exception instead of needing to wrap it with RuntimeException
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    /**
     * Customizes the YB master container's command in a subclass
     *
     * @param command the default command parts to use for the container
     * @return modified command parts
     */
    protected List<String> customizeMasterCommand(List<String> command) {
        return command;
    }

    /**
     * Customizes the YB tserver container's command in a subclass
     *
     * @param command the default command parts to use for the container
     * @return modified command parts
     */
    protected List<String> customizeTserverCommand(List<String> command) {
        return command;
    }

    /**
     * Customizes YB ymaster and tserver containers before starting the containers
     *
     * @param ymasterContainer the YB master container
     * @param tserverContainer the YB tserver container
     */
    protected void customizeContainers(GenericContainer<?> ymasterContainer, GenericContainer<?> tserverContainer) {

    }

    @Override
    public synchronized void stop() {
        if (initialized.compareAndSet(true, false)) {
            tserverContainer.stop();
            tserverContainer = null;
            ymasterContainer.stop();
            ymasterContainer = null;
            network.close();
            network = null;
        }
    }

    public String getYCQLHost() {
        return tserverContainer.getContainerIpAddress();
    }

    public int getYCQLPort() {
        return tserverContainer.getMappedPort(YCQL_SERVICE_PORT);
    }

    public String getYSQLHost() {
        return tserverContainer.getContainerIpAddress();
    }

    public int getYSQLPort() {
        return tserverContainer.getMappedPort(YSQL_SERVICE_PORT);
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

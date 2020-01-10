package com.github.lhotari.spring.dbcontainers;

import com.github.lhotari.dbcontainer.DatabaseContainer;
import com.github.lhotari.dbcontainer.yugabyte.LoggingYugaByteDatabaseContainer;
import com.github.lhotari.dbcontainer.yugabyte.YugaByteDatabaseContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Spring ApplicationContextInitializer that starts a YugaByte container with a mounted logs and core dump directory
 */
public class LoggingYugaByteSpringTestContextInitializer extends DatabaseContainerInitializingSpringTestContextInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingYugaByteSpringTestContextInitializer.class);

    @Override
    protected DatabaseContainer createDatabaseContainer(ConfigurableEnvironment environment) {
        Path logsAndCoresDirectory = resolveLogsPath();
        LOG.info("Logs and directories are in " + logsAndCoresDirectory.toAbsolutePath());
        YugaByteDatabaseContainer yugaByteDatabaseContainer = new LoggingYugaByteDatabaseContainer(logsAndCoresDirectory);
        return yugaByteDatabaseContainer;
    }

    protected Path resolveLogsPath() {
        Path logsAndCoresDirectory = null;
        try {
            logsAndCoresDirectory = Files.createTempDirectory("yugabyte");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return logsAndCoresDirectory;
    }
}

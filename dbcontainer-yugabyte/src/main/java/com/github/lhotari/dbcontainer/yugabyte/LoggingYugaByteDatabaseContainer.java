package com.github.lhotari.dbcontainer.yugabyte;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * YugaByte {@link com.github.lhotari.dbcontainer.DatabaseContainer} implementation that logs to a mounted directory.
 * Besides logs, the /cores directory within the container is mounted.
 *
 * This assumes "echo '/cores/core.%e.%p' | sudo tee /proc/sys/kernel/core_pattern" configuration on the Linux host
 * to capture core dumps in the mounted directory.
 */
public class LoggingYugaByteDatabaseContainer extends YugaByteDatabaseContainer {
    // the coredump directory within the container
    // assumes "echo '/cores/core.%e.%p' | sudo tee /proc/sys/kernel/core_pattern" on host
    static final String CORE_DUMP_DIRECTORY_IN_CONTAINER = "/cores";
    static final String LOGS_DIRECTORY_IN_CONTAINER = "/yblogs";
    private final Path logsAndCoresDirectory;

    public LoggingYugaByteDatabaseContainer(Path logsAndCoresRootDirectory) {
        this.logsAndCoresDirectory = logsAndCoresRootDirectory;
    }

    public Path getLogsAndCoresDirectory() {
        return logsAndCoresDirectory;
    }

    @Override
    protected void customizeContainers(GenericContainer<?> ymasterContainer, GenericContainer<?> tserverContainer) {
        bindDirectories(ymasterContainer, tserverContainer);
    }

    @Override
    protected List<String> customizeMasterCommand(List<String> command) {
        return logToYblogs(command);
    }

    @Override
    protected List<String> customizeTserverCommand(List<String> command) {
        return logToYblogs(command);
    }

    private List<String> logToYblogs(List<String> command) {
        command.remove("--logtostderr");
        command.add("--log_dir");
        command.add(LOGS_DIRECTORY_IN_CONTAINER);
        return command;
    }

    private void bindDirectories(GenericContainer<?> ymasterContainer, GenericContainer<?> tserverContainer) {
        Path yblogsDir = logsAndCoresDirectory.resolve("logs");
        bindLogsDir(ymasterContainer, yblogsDir, "master");
        bindLogsDir(tserverContainer, yblogsDir, "tserver");
        Path ybcoresDir = logsAndCoresDirectory.resolve("cores");
        bindCoresDir(ymasterContainer, ybcoresDir, "master");
        bindCoresDir(tserverContainer, ybcoresDir, "tserver");
    }

    private void bindCoresDir(GenericContainer<?> container, Path ybcoresDir, String subdirName) {
        bindDirectory(container, ybcoresDir, subdirName, CORE_DUMP_DIRECTORY_IN_CONTAINER);
    }

    private void bindLogsDir(GenericContainer<?> container, Path yblogsDir, String subdirName) {
        bindDirectory(container, yblogsDir, subdirName, LOGS_DIRECTORY_IN_CONTAINER);
    }

    private void bindDirectory(GenericContainer<?> container, Path parentDirectory, String subdirName, String containerPath) {
        File coreDir = parentDirectory.resolve(subdirName).toFile();
        coreDir.mkdirs();
        container.addFileSystemBind(coreDir.getAbsolutePath(), containerPath, BindMode.READ_WRITE);
    }
}

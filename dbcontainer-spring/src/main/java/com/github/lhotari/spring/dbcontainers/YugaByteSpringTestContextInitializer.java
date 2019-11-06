package com.github.lhotari.spring.dbcontainers;

import com.github.lhotari.dbcontainer.DatabaseContainer;
import com.github.lhotari.dbcontainer.yugabyte.YugaByteDatabaseContainer;
import org.springframework.core.env.ConfigurableEnvironment;

public class YugaByteSpringTestContextInitializer extends DatabaseContainerInitializingSpringTestContextInitializer {
    @Override
    protected DatabaseContainer createDatabaseContainer(ConfigurableEnvironment environment) {
        YugaByteDatabaseContainer yugaByteDatabaseContainer = new YugaByteDatabaseContainer();
        yugaByteDatabaseContainer.start();
        return yugaByteDatabaseContainer;
    }
}

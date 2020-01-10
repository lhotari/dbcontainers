package com.github.lhotari.spring.dbcontainers;

import com.github.lhotari.dbcontainer.DatabaseContainer;
import com.github.lhotari.dbcontainer.postgres.PostgresDatabaseContainer;
import org.springframework.core.env.ConfigurableEnvironment;

public class PostgresSpringTestContextInitializer extends DatabaseContainerInitializingSpringTestContextInitializer {
    @Override
    protected DatabaseContainer createDatabaseContainer(ConfigurableEnvironment environment) {
        return new PostgresDatabaseContainer();
    }
}

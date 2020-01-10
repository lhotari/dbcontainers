package com.github.lhotari.spring.dbcontainers;

import com.github.lhotari.dbcontainer.DatabaseContainer;
import org.springframework.context.*;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

abstract public class DatabaseContainerInitializingSpringTestContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        DatabaseContainer databaseContainer = createDatabaseContainer(applicationContext.getEnvironment());
        databaseContainer.start();
        applicationContext.addApplicationListener(new ApplicationListener<ContextClosedEvent>() {
            @Override
            public void onApplicationEvent(ContextClosedEvent event) {
                databaseContainer.stop();
            }
        });
        Map<String, Object> applicationPropertiesForDatabase = createApplicationPropertiesForDatabase(databaseContainer);
        MapPropertySource propertySource = new MapPropertySource(getClass().getName(), applicationPropertiesForDatabase);
        applicationContext.getEnvironment().getPropertySources().addFirst(propertySource);
    }

    protected abstract DatabaseContainer createDatabaseContainer(ConfigurableEnvironment environment);

    private Map<String, Object> createApplicationPropertiesForDatabase(DatabaseContainer databaseContainer) {
        Map<String, Object> map = new HashMap<>();
        map.put("spring.datasource.url", databaseContainer.getJdbcUrl());
        map.put("spring.datasource.username", databaseContainer.getDatabaseUser());
        map.put("spring.datasource.password", databaseContainer.getDatabasePassword());
        map.put("spring.r2dbc.url", databaseContainer.getR2dbcUrl());
        map.put("spring.r2dbc.username", databaseContainer.getDatabaseUser());
        map.put("spring.r2dbc.password", databaseContainer.getDatabasePassword());
        return map;
    }
}

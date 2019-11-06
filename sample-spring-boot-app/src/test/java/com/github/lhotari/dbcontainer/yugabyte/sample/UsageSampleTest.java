package com.github.lhotari.dbcontainer.yugabyte.sample;

import com.github.lhotari.spring.dbcontainers.YugaByteSpringTestContextInitializer;
import io.r2dbc.postgresql.codec.Json;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.mapping.SettableValue;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;

@SpringBootTest(classes = UsageSampleTest.TestApplication.class)
@ContextConfiguration(initializers = YugaByteSpringTestContextInitializer.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UsageSampleTest {
    @SpringBootApplication
    static class TestApplication {

    }

    @Autowired
    JdbcOperations jdbcOperations;

    @Autowired
    DatabaseClient databaseClient;

    @BeforeAll
    void createTable() {
        jdbcOperations.execute("DROP TABLE IF EXISTS my_table");
        jdbcOperations.execute("CREATE TABLE my_table (my_json JSON)");
    }

    @Test
    void shouldSupportJsonType() {
        databaseClient.insert().into("my_table")
                .value("my_json", SettableValue.from(Json.of("{\"hello\": \"world\"}")))
                .fetch().rowsUpdated()
                .as(StepVerifier::create)
                .expectNext(1)
                .verifyComplete();

        databaseClient.execute("select * from my_table")
                .fetch()
                .first()
                .map(it -> ((Json) it.get("my_json")).asString())
                .as(StepVerifier::create)
                .expectNext("{\"hello\": \"world\"}")
                .verifyComplete();
    }

}

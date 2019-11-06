package com.github.lhotari.dbcontainer.yugabyte.sample;

import com.example.demo.CityRepository;
import com.example.demo.DemoApplication;
import com.github.lhotari.spring.dbcontainers.YugaByteSpringTestContextInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

@SpringBootTest(classes = DemoApplication.class)
@ContextConfiguration(initializers = YugaByteSpringTestContextInitializer.class)
@TestPropertySource(properties = {"spring.r2dbc.initialization-mode=always", "logger.level.root=DEBUG"})
class YugabyteR2dbcSampleTests {
    @Autowired
    CityRepository cityRepository;
    @Autowired
    DatabaseClient databaseClient;

    @Test
    void shouldCityRepositoryReturnCreatedCities() {
        cityRepository.findAll()
                .as(StepVerifier::create)
                .expectNextMatches(city -> city.getName().equals("Washington"))
                .expectComplete()
                .verify();
    }

    @Test
    void shouldContainRowInCityTable() {
        databaseClient.execute("select * from city")
                .fetch()
                .first()
                .as(StepVerifier::create)
                .expectNextMatches(fields -> fields.get("NAME").equals("Washington"))
                .expectComplete()
                .verify();
    }
}

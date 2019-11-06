/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.demo;

import com.github.lhotari.spring.dbcontainers.YugaByteSpringTestContextInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Collections;

@SpringBootApplication
@EnableTransactionManagement
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(DemoApplication.class);
        // Use an embedded YugaByte database controlled by Testcontainers
        springApplication.addInitializers(new YugaByteSpringTestContextInitializer());
        springApplication.setDefaultProperties(Collections.singletonMap("spring.r2dbc.initialization-mode", "always"));
        springApplication.run(args);
    }

}

package com.symphonyforgeops.api.interfaces.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TestDatabaseIsolationTests {

    @Autowired
    private Environment environment;

    @Test
    void integrationTestsUseDedicatedDatabaseByDefault() {
        String datasourceUrl = environment.getRequiredProperty("spring.datasource.url");

        assertThat(datasourceUrl).contains("forgeops_test");
        assertThat(datasourceUrl).doesNotContain("localhost:3306/forgeops?");
    }
}

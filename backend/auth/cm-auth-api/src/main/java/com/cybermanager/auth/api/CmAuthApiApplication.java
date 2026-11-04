package com.cybermanager.auth.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.cybermanager.auth")
@EntityScan(basePackages = "com.cybermanager.auth.infrastructure.out.entities")
@EnableJpaRepositories(basePackages = "com.cybermanager.auth.infrastructure.out.adapters.persistences.repositories")
public class CmAuthApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmAuthApiApplication.class, args);
    }

}


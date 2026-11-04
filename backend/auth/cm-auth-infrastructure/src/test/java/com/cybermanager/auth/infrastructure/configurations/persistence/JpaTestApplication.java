package com.cybermanager.auth.infrastructure.configurations.persistence;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan(basePackages = "com.cybermanager.auth.infrastructure.out.entities")
@EnableJpaRepositories(basePackages = "com.cybermanager.auth.infrastructure.out.adapters.persistences.repositories")
class JpaTestApplication {

}

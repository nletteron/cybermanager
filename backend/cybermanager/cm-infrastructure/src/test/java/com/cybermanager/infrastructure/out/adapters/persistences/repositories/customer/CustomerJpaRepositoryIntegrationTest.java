package com.cybermanager.infrastructure.out.adapters.persistences.repositories.customer;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.customer.CustomerJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private CustomerJpaRepository repository;

    @Test
    void shouldSearchCustomersByNameAndTypeOrderedByName() {
        repository.save(customer("Bob", "WALK_IN"));
        repository.save(customer("Alice", "SUBSCRIBER"));

        assertEquals("Alice", repository.findAllByOrderByNameAsc().get(0).name);
        assertEquals(1, repository.findByNameContainingIgnoreCaseOrderByNameAsc("ali").size());
        assertEquals(1, repository.findByTypeOrderByNameAsc("SUBSCRIBER").size());
        assertEquals(1, repository.findByTypeAndNameContainingIgnoreCaseOrderByNameAsc("SUBSCRIBER", "ali").size());
    }

    @Test
    void shouldReturnEmptyWhenNoCustomerMatches() {
        assertTrue(repository.findByNameContainingIgnoreCaseOrderByNameAsc("missing").isEmpty());
    }

    private CustomerJpaEntity customer(String name, String type) {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.id = UUID.randomUUID();
        entity.name = name;
        entity.type = type;
        entity.status = "ACTIVE";
        entity.remainingMinutes = 0;
        return entity;
    }

}

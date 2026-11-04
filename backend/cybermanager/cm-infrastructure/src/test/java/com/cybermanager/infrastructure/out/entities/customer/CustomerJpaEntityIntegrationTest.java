package com.cybermanager.infrastructure.out.entities.customer;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.customer.CustomerJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private CustomerJpaRepository repository;

    @Test
    void shouldPersistCustomerEntityFields() {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.id = UUID.randomUUID();
        entity.name = "Alice";
        entity.type = "SUBSCRIBER";
        entity.status = "ACTIVE";
        entity.remainingMinutes = 90;
        entity.comment = "Commentaire client\nDeuxieme ligne";

        repository.saveAndFlush(entity);

        CustomerJpaEntity saved = repository.findById(entity.id).orElseThrow();
        assertEquals("SUBSCRIBER", saved.type);
        assertEquals(90, saved.remainingMinutes);
        assertEquals("Commentaire client\nDeuxieme ligne", saved.comment);
    }

}

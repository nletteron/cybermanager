package com.cybermanager.infrastructure.out.adapters.persistences.repositories.subscription;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.subscription.SubscriptionOfferJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscriptionOfferJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private SubscriptionOfferJpaRepository repository;

    @Test
    void shouldPersistAndFindSubscriptionOffer() {
        SubscriptionOfferJpaEntity entity = new SubscriptionOfferJpaEntity();
        entity.id = UUID.randomUUID();
        entity.name = "Forfait 2h";
        entity.price = new BigDecimal("7.50");
        entity.includedMinutes = 120;
        entity.status = "ACTIVE";

        repository.save(entity);

        assertTrue(repository.findById(entity.id).isPresent());
        assertEquals("Forfait 2h", repository.findAll().get(0).name);
    }

    @Test
    void shouldReturnEmptyWhenOfferDoesNotExist() {
        assertTrue(repository.findById(UUID.randomUUID()).isEmpty());
    }

}

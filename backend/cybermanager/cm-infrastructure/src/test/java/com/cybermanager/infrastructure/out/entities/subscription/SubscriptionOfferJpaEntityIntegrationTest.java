package com.cybermanager.infrastructure.out.entities.subscription;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.subscription.SubscriptionOfferJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubscriptionOfferJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private SubscriptionOfferJpaRepository repository;

    @Test
    void shouldPersistSubscriptionOfferEntityFields() {
        SubscriptionOfferJpaEntity entity = new SubscriptionOfferJpaEntity();
        entity.id = UUID.randomUUID();
        entity.name = "Forfait 2h";
        entity.price = new BigDecimal("7.50");
        entity.includedMinutes = 120;
        entity.status = "ACTIVE";

        repository.saveAndFlush(entity);

        SubscriptionOfferJpaEntity saved = repository.findById(entity.id).orElseThrow();
        assertEquals(120, saved.includedMinutes);
        assertEquals("ACTIVE", saved.status);
    }

}

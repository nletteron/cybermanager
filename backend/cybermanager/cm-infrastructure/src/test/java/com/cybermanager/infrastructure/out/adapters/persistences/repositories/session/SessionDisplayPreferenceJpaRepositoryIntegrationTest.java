package com.cybermanager.infrastructure.out.adapters.persistences.repositories.session;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.session.SessionDisplayPreferenceJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class SessionDisplayPreferenceJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private SessionDisplayPreferenceJpaRepository repository;

    @Test
    void shouldReadByOwnerAndKeepOneRowWhenTheChoiceChanges() {
        var entity = new SessionDisplayPreferenceJpaEntity();
        entity.userEmail = "alice@example.test";
        entity.groupEntries = true;
        repository.saveAndFlush(entity);
        assertTrue(repository.findById(entity.userEmail).orElseThrow().groupEntries);
        assertTrue(repository.findById("bob@example.test").isEmpty());

        entity.groupEntries = false;
        repository.saveAndFlush(entity);
        assertFalse(repository.findById(entity.userEmail).orElseThrow().groupEntries);
        assertEquals(1, repository.count());
    }

}

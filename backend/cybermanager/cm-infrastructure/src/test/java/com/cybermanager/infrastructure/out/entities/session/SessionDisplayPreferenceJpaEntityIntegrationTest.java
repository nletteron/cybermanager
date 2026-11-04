package com.cybermanager.infrastructure.out.entities.session;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

class SessionDisplayPreferenceJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private TestEntityManager manager;

    @Test
    void shouldPersistEmailAndGroupingAcrossPersistenceContextReloads() {
        var entity = new SessionDisplayPreferenceJpaEntity();
        entity.userEmail = "alice@example.test";
        entity.groupEntries = true;
        manager.persistAndFlush(entity);
        manager.clear();

        var restored = manager.find(SessionDisplayPreferenceJpaEntity.class, entity.userEmail);
        assertEquals(entity.userEmail, restored.userEmail);
        assertTrue(restored.groupEntries);
        restored.groupEntries = false;
        manager.flush();
        manager.clear();
        assertFalse(manager.find(SessionDisplayPreferenceJpaEntity.class, entity.userEmail).groupEntries);
    }

}

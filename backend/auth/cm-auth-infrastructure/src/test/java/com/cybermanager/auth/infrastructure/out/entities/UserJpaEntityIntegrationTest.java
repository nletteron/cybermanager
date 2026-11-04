package com.cybermanager.auth.infrastructure.out.entities;

import com.cybermanager.auth.infrastructure.configurations.persistence.AbstractAuthJpaTest;
import com.cybermanager.auth.infrastructure.out.adapters.persistences.repositories.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserJpaEntityIntegrationTest extends AbstractAuthJpaTest {

    @Autowired
    private UserJpaRepository repository;

    @Test
    void shouldPersistUserEntityWithRoles() {
        var entity = user(UUID.randomUUID(), "admin@cybermanager.local", UserStatusJpa.ACTIVE, Set.of(AppRoleJpa.ADMIN));

        repository.saveAndFlush(entity);

        UserJpaEntity saved = repository.findById(entity.getId()).orElseThrow();
        assertEquals(UserStatusJpa.ACTIVE, saved.getStatus());
        assertEquals(Set.of(AppRoleJpa.ADMIN), saved.getRoles());
    }

    @Test
    void shouldRejectUserWithoutRequiredEmail() {
        var entity = user(UUID.randomUUID(), null, UserStatusJpa.ACTIVE, Set.of(AppRoleJpa.ADMIN));

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(entity));
    }

}

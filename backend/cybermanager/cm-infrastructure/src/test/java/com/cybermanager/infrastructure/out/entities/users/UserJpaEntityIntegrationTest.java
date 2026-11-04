package com.cybermanager.infrastructure.out.entities.users;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.users.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private UserJpaRepository repository;

    @Test
    void shouldPersistUserEntityWithRoles() {
        UserJpaEntity entity = user("admin@cybermanager.local");

        repository.saveAndFlush(entity);

        UserJpaEntity saved = repository.findById(entity.getId()).orElseThrow();
        assertEquals(UserStatusJpa.ACTIVE, saved.getStatus());
        assertEquals(Set.of(AppRoleJpa.ADMIN), saved.getRoles());
    }

    @Test
    void shouldRejectUserEntityWithoutRequiredEmail() {
        UserJpaEntity entity = user(null);

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(entity));
    }

    private UserJpaEntity user(String email) {

        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setEmail(email);
        entity.setFirstName("Admin");
        entity.setLastName("User");
        entity.setPasswordHash("hash");
        entity.setStatus(UserStatusJpa.ACTIVE);
        entity.setRoles(Set.of(AppRoleJpa.ADMIN));
        return entity;

    }

}

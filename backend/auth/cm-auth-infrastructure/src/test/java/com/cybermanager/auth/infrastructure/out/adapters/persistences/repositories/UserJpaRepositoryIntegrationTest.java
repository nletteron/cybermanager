package com.cybermanager.auth.infrastructure.out.adapters.persistences.repositories;

import com.cybermanager.auth.infrastructure.configurations.persistence.AbstractAuthJpaTest;
import com.cybermanager.auth.infrastructure.out.entities.AppRoleJpa;
import com.cybermanager.auth.infrastructure.out.entities.UserStatusJpa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserJpaRepositoryIntegrationTest extends AbstractAuthJpaTest {

    @Autowired
    private UserJpaRepository repository;

    @Test
    void shouldFindUserByEmail() {
        repository.save(user(UUID.randomUUID(), "admin@cybermanager.local", UserStatusJpa.ACTIVE, Set.of(AppRoleJpa.ADMIN)));

        assertTrue(repository.findByEmail("admin@cybermanager.local").isPresent());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        repository.save(user(UUID.randomUUID(), "duplicate@cybermanager.local", UserStatusJpa.ACTIVE, Set.of(AppRoleJpa.ADMIN)));
        repository.save(user(UUID.randomUUID(), "duplicate@cybermanager.local", UserStatusJpa.ACTIVE, Set.of(AppRoleJpa.EMPLOYEE)));

        assertThrows(DataIntegrityViolationException.class, () -> repository.flush());
    }

}

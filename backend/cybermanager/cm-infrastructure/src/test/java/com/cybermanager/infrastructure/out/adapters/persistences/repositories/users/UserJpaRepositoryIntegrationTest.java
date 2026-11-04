package com.cybermanager.infrastructure.out.adapters.persistences.repositories.users;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.users.AppRoleJpa;
import com.cybermanager.infrastructure.out.entities.users.UserJpaEntity;
import com.cybermanager.infrastructure.out.entities.users.UserStatusJpa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private UserJpaRepository repository;

    @Test
    void shouldQueryUsersByEmailAndStatus() {
        UserJpaEntity user = user("admin@cybermanager.local", UserStatusJpa.ACTIVE);
        repository.save(user);

        assertTrue(repository.existsByEmail(user.getEmail()));
        assertTrue(repository.findByEmail(user.getEmail()).isPresent());
        assertFalse(repository.existsByEmailAndIdNot(user.getEmail(), user.getId()));
        assertEquals(user.getEmail(), repository.findByStatus(UserStatusJpa.ACTIVE).get(0).getEmail());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        repository.save(user("duplicate@cybermanager.local", UserStatusJpa.ACTIVE));
        repository.save(user("duplicate@cybermanager.local", UserStatusJpa.DISABLED));

        assertThrows(DataIntegrityViolationException.class, () -> repository.flush());
    }

    private UserJpaEntity user(String email, UserStatusJpa status) {

        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setEmail(email);
        entity.setFirstName("Admin");
        entity.setLastName("User");
        entity.setPasswordHash("hash");
        entity.setStatus(status);
        entity.setRoles(Set.of(AppRoleJpa.ADMIN));
        return entity;

    }

}

package com.cybermanager.auth.infrastructure.out.adapters.persistences;

import com.cybermanager.auth.domain.valueobjects.EmailAddress;
import com.cybermanager.auth.domain.valueobjects.UserId;
import com.cybermanager.auth.infrastructure.configurations.persistence.AbstractAuthJpaTest;
import com.cybermanager.auth.infrastructure.out.adapters.persistences.repositories.UserJpaRepository;
import com.cybermanager.auth.infrastructure.out.entities.AppRoleJpa;
import com.cybermanager.auth.infrastructure.out.entities.UserStatusJpa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserAuthenticationRepositoryAdapterIntegrationTest extends AbstractAuthJpaTest {

    @Autowired
    private UserJpaRepository repository;
    @Autowired
    private UserAuthenticationRepositoryAdapter adapter;

    @Test
    void shouldFindUserAccountByEmailWithRoles() {
        UUID id = UUID.randomUUID();
        repository.save(user(id, "admin@cybermanager.local", UserStatusJpa.ACTIVE, Set.of(AppRoleJpa.ADMIN, AppRoleJpa.EMPLOYEE)));

        var result = adapter.findByEmail(new EmailAddress("ADMIN@cybermanager.local"));

        assertTrue(result.isPresent());
        assertEquals(new UserId(id), result.get().id());
        assertEquals("admin@cybermanager.local", result.get().email().value());
        assertEquals(2, result.get().roles().size());
        assertTrue(result.get().isActive());
    }

    @Test
    void shouldFindUserAccountByIdAndReturnEmptyWhenMissing() {
        UUID id = UUID.randomUUID();
        repository.save(user(id, "employee@cybermanager.local", UserStatusJpa.DISABLED, Set.of(AppRoleJpa.EMPLOYEE)));

        var found = adapter.findById(new UserId(id));
        var missing = adapter.findByEmail(new EmailAddress("missing@cybermanager.local"));

        assertTrue(found.isPresent());
        assertEquals("Employee", found.get().firstName());
        assertTrue(missing.isEmpty());
    }

}

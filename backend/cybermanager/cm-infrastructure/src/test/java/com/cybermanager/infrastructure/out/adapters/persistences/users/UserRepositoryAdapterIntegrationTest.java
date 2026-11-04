package com.cybermanager.infrastructure.out.adapters.persistences.users;

import com.cybermanager.domain.valueobjects.users.AppRole;
import com.cybermanager.domain.valueobjects.users.EmailAddress;
import com.cybermanager.domain.valueobjects.users.UserId;
import com.cybermanager.domain.valueobjects.users.UserStatus;
import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryAdapterIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private UserRepositoryAdapter adapter;

    @Test
    void shouldSaveFindSearchAndDeleteUsersWithRoles() {
        var active = adapter.save(user("admin@cybermanager.local", "Admin", Set.of(AppRole.ADMIN)));
        var disabled = adapter.save(user("employee@cybermanager.local", "Employee", Set.of(AppRole.EMPLOYEE)).disable());

        var activeUsers = adapter.search("ADMIN", UserStatus.ACTIVE);
        adapter.deleteById(disabled.id());

        assertEquals(1, activeUsers.size());
        assertEquals(active.id(), activeUsers.get(0).id());
        assertTrue(adapter.findById(active.id()).isPresent());
        assertTrue(adapter.findById(disabled.id()).isEmpty());
        assertTrue(adapter.existsByEmail(active.email()));
        assertFalse(adapter.existsByEmailAndIdNot(active.email(), active.id()));
        assertTrue(adapter.existsByEmailAndIdNot(active.email(), disabled.id()));
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {
        assertTrue(adapter.findById(UserId.newId()).isEmpty());
        assertFalse(adapter.existsByEmail(new EmailAddress("missing@cybermanager.local")));
    }

}

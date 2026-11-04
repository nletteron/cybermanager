package com.cybermanager.auth.domain.businessobjects;

import com.cybermanager.auth.domain.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserAccountTest {

    @Test
    void shouldCreateActiveUserAccountWithTrimmedNames() {
        UserAccount result = userAccount(UserStatus.ACTIVE);

        assertEquals("Alice", result.firstName());
        assertEquals("Martin", result.lastName());
        assertTrue(result.isActive());
        assertEquals(Set.of(AppRole.ADMIN), result.roles());
    }

    @Test
    void shouldReportDisabledAccountAsInactive() {
        UserAccount result = userAccount(UserStatus.DISABLED);

        assertFalse(result.isActive());
    }

    @Test
    void shouldRejectBlankFirstName() {
        assertThrows(IllegalArgumentException.class, () -> new UserAccount(
                new UserId(UUID.randomUUID()),
                new EmailAddress("admin@cybermanager.local"),
                " ",
                "Martin",
                new PasswordHash("hashed"),
                Set.of(AppRole.ADMIN),
                UserStatus.ACTIVE
        ));
    }

    @Test
    void shouldRejectEmptyRoles() {
        assertThrows(IllegalArgumentException.class, () -> new UserAccount(
                new UserId(UUID.randomUUID()),
                new EmailAddress("admin@cybermanager.local"),
                "Alice",
                "Martin",
                new PasswordHash("hashed"),
                Set.of(),
                UserStatus.ACTIVE
        ));
    }

    private UserAccount userAccount(UserStatus status) {
        return new UserAccount(
                new UserId(UUID.randomUUID()),
                new EmailAddress("admin@cybermanager.local"),
                " Alice ",
                " Martin ",
                new PasswordHash("hashed"),
                Set.of(AppRole.ADMIN),
                status
        );
    }

}

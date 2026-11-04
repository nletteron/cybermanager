package com.cybermanager.domain.businessobjects.users;

import com.cybermanager.domain.valueobjects.users.*;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateActiveUserWithTrimmedNames() {
        User result = User.create(
                new UserId(UUID.randomUUID()),
                new EmailAddress("employee@cybermanager.local"),
                " Alice ",
                " Martin ",
                new PasswordHash("hashed"),
                Set.of(AppRole.EMPLOYEE)
        );

        assertEquals("Alice", result.firstName());
        assertEquals("Martin", result.lastName());
        assertEquals(UserStatus.ACTIVE, result.status());
        assertTrue(result.hasRole(AppRole.EMPLOYEE));
    }

    @Test
    void shouldKeepCurrentPasswordHashWhenUpdatePasswordIsNull() {
        User current = user();

        User result = current.update(
                new EmailAddress("new@cybermanager.local"),
                "Bob",
                "Durand",
                Set.of(AppRole.ADMIN),
                null
        );

        assertEquals(current.id(), result.id());
        assertEquals(current.passwordHash(), result.passwordHash());
        assertEquals("new@cybermanager.local", result.email().value());
        assertTrue(result.hasRole(AppRole.ADMIN));
    }

    @Test
    void shouldRejectEmptyRoles() {
        assertThrows(IllegalArgumentException.class, () -> User.create(
                new UserId(UUID.randomUUID()),
                new EmailAddress("employee@cybermanager.local"),
                "Alice",
                "Martin",
                new PasswordHash("hashed"),
                Set.of()
        ));
    }

    @Test
    void shouldEnableAndDisableUser() {
        User disabled = user().disable();
        User enabled = disabled.enable();

        assertEquals(UserStatus.DISABLED, disabled.status());
        assertEquals(UserStatus.ACTIVE, enabled.status());
    }

    private User user() {
        return User.create(
                new UserId(UUID.randomUUID()),
                new EmailAddress("employee@cybermanager.local"),
                "Alice",
                "Martin",
                new PasswordHash("hashed"),
                Set.of(AppRole.EMPLOYEE)
        );
    }

}

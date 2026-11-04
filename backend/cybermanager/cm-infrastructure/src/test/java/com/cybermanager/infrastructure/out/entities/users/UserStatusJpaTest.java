package com.cybermanager.infrastructure.out.entities.users;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserStatusJpaTest {

    @Test
    void shouldExposeExpectedPersistenceStatusNames() {
        assertEquals("ACTIVE", UserStatusJpa.ACTIVE.name());
        assertEquals("DISABLED", UserStatusJpa.DISABLED.name());
    }

}

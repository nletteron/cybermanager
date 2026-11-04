package com.cybermanager.auth.infrastructure.out.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserStatusJpaTest {

    @Test
    void shouldExposeExpectedPersistenceStatusNames() {
        assertEquals("ACTIVE", UserStatusJpa.ACTIVE.name());
        assertEquals("DISABLED", UserStatusJpa.DISABLED.name());
    }

}

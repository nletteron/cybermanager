package com.cybermanager.infrastructure.out.entities.users;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppRoleJpaTest {

    @Test
    void shouldExposeExpectedPersistenceRoleNames() {
        assertEquals("ADMIN", AppRoleJpa.ADMIN.name());
        assertEquals("EMPLOYEE", AppRoleJpa.EMPLOYEE.name());
    }

}

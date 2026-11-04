package com.cybermanager.infrastructure.out.adapters.persistences;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConnectionTargetIntegrationTest {

    @Test
    void shouldParsePostgreSqlJdbcUrl() {
        ConnectionTarget result = ConnectionTarget.from("jdbc:postgresql://localhost:5544/cybermanager");

        assertEquals("localhost", result.host());
        assertEquals(5544, result.port());
        assertEquals("cybermanager", result.database());
    }

    @Test
    void shouldUseDefaultPostgreSqlPort() {
        ConnectionTarget result = ConnectionTarget.from("jdbc:postgresql://localhost/cybermanager");

        assertEquals(5432, result.port());
    }

    @Test
    void shouldRejectInvalidJdbcUrl() {
        assertThrows(IllegalArgumentException.class, () -> ConnectionTarget.from("jdbc:h2:mem:test"));
    }

}

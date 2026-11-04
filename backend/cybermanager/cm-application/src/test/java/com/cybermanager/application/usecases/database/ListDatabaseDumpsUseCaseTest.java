package com.cybermanager.application.usecases.database;

import com.cybermanager.application.ports.database.DatabaseBackupGateway;
import com.cybermanager.application.ports.database.DumpResource;
import com.cybermanager.domain.queries.database.ListDatabaseDumpsQuery;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListDatabaseDumpsUseCaseTest {

    @Test
    void shouldListDumpsForAdministrator() {
        DatabaseBackupGateway gateway = mock(DatabaseBackupGateway.class);
        ListDatabaseDumpsUseCase useCase = new ListDatabaseDumpsUseCase(gateway);
        Instant modifiedAt = Instant.parse("2026-08-06T12:00:00Z");

        when(gateway.list()).thenReturn(List.of(new DumpResource("backup.dump", 42L, modifiedAt)));

        var result = useCase.execute(new ListDatabaseDumpsQuery("admin@cybermanager.local", Set.of("ADMIN")));

        assertEquals(1, result.size());
        assertEquals("backup.dump", result.getFirst().fileName());
        assertEquals(42L, result.getFirst().size());
    }

}

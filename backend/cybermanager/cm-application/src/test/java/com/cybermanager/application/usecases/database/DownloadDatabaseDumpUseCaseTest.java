package com.cybermanager.application.usecases.database;

import com.cybermanager.application.ports.database.DatabaseBackupGateway;
import com.cybermanager.application.ports.database.DumpContent;
import com.cybermanager.domain.queries.database.DownloadDatabaseDumpQuery;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DownloadDatabaseDumpUseCaseTest {

    @Test
    void shouldDownloadSelectedDump() {
        DatabaseBackupGateway gateway = mock(DatabaseBackupGateway.class);
        DownloadDatabaseDumpUseCase useCase = new DownloadDatabaseDumpUseCase(gateway);
        ByteArrayInputStream content = new ByteArrayInputStream("dump".getBytes());

        when(gateway.read("backup.dump"))
                .thenReturn(new DumpContent("backup.dump", 4L, content));

        var result = useCase.execute(new DownloadDatabaseDumpQuery(
                "admin@cybermanager.local",
                Set.of("ADMIN"),
                "backup.dump"
        ));

        assertEquals("backup.dump", result.fileName());
        assertEquals(4L, result.size());
        assertEquals(content, result.content());
    }

}

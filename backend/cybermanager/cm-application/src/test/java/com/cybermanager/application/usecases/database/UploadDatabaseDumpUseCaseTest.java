package com.cybermanager.application.usecases.database;

import com.cybermanager.application.ports.database.DatabaseBackupGateway;
import com.cybermanager.application.ports.database.DumpResource;
import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.domain.commands.database.UploadDatabaseDumpCommand;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UploadDatabaseDumpUseCaseTest {

    @Test
    void shouldUploadDumpWhenCommandIsValid() {
        DatabaseBackupGateway gateway = mock(DatabaseBackupGateway.class);
        UploadDatabaseDumpUseCase useCase = new UploadDatabaseDumpUseCase(gateway);
        ByteArrayInputStream content = new ByteArrayInputStream("dump".getBytes());
        Instant modifiedAt = Instant.parse("2026-08-06T13:00:00Z");

        when(gateway.store("upload.dump", content))
                .thenReturn(new DumpResource("upload.dump", 4L, modifiedAt));

        var result = useCase.execute(new UploadDatabaseDumpCommand(
                "admin@cybermanager.local",
                Set.of("ADMIN"),
                "upload.dump",
                content
        ));

        assertEquals("upload.dump", result.fileName());
        assertEquals(4L, result.size());
        assertEquals(modifiedAt, result.modifiedAt());
    }

    @Test
    void shouldRejectUploadWithoutFileName() {
        DatabaseBackupGateway gateway = mock(DatabaseBackupGateway.class);
        UploadDatabaseDumpUseCase useCase = new UploadDatabaseDumpUseCase(gateway);

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(
                new UploadDatabaseDumpCommand(
                        "admin@cybermanager.local",
                        Set.of("ADMIN"),
                        " ",
                        new ByteArrayInputStream("dump".getBytes())
                )
        ));

        assertEquals(BusinessErrorType.VALIDATION, exception.type());
        assertEquals("DATABASE_DUMP_REQUIRED", exception.code());
    }

}

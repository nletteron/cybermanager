package com.cybermanager.application.usecases.database;

import com.cybermanager.application.ports.database.DatabaseBackupGateway;
import com.cybermanager.domain.commands.database.RestoreDatabaseDumpCommand;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RestoreDatabaseDumpUseCaseTest {

    @Test
    void shouldRestoreSelectedDump() {
        DatabaseBackupGateway gateway = mock(DatabaseBackupGateway.class);
        RestoreDatabaseDumpUseCase useCase = new RestoreDatabaseDumpUseCase(gateway);

        useCase.execute(new RestoreDatabaseDumpCommand(
                "admin@cybermanager.local",
                Set.of("ADMIN"),
                "backup.dump"
        ));

        verify(gateway).restore("backup.dump");
    }

}

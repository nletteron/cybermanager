package com.cybermanager.application.usecases.database;

import com.cybermanager.application.ports.database.DatabaseBackupGateway;
import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.domain.commands.database.ExportDatabaseCommand;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ExportDatabaseUseCaseTest {

    @Test
    void shouldRejectExportForEmployee() {
        DatabaseBackupGateway gateway = mock(DatabaseBackupGateway.class);
        ExportDatabaseUseCase useCase = new ExportDatabaseUseCase(gateway);

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(
                new ExportDatabaseCommand("employee@cybermanager.local", Set.of("EMPLOYEE"))
        ));

        assertEquals(BusinessErrorType.FORBIDDEN, exception.type());
        assertEquals("ADMIN_ROLE_REQUIRED", exception.code());
    }

}

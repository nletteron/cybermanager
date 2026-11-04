package com.cybermanager.infrastructure.out.adapters.persistences;

import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PostgreSqlContainerDumpInputStreamTest {

    @Test
    void shouldTransferBinaryBytesAndVerifyCommandAtEnd() throws Exception {
        Process process = mock(Process.class);
        byte[] bytes = new byte[]{0, (byte) 255, 1};
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        var commands = mock(PostgreSqlContainerCommands.class);
        Path error = Path.of("diagnostic.log");
        var output = new ByteArrayOutputStream();

        try (var input = new PostgreSqlContainerDumpInputStream(process, error, commands, BusinessErrorCode.DATABASE_DUMP_READ_FAILED)) {
            input.transferTo(output);
        }

        assertArrayEquals(bytes, output.toByteArray());
        verify(commands).check(process, error, BusinessErrorCode.DATABASE_DUMP_READ_FAILED);
        verify(commands).delete(error);
    }

    @Test
    void shouldCancelProcessWhenClientClosesDownload() throws Exception {
        Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2}));
        when(process.isAlive()).thenReturn(true);
        var commands = mock(PostgreSqlContainerCommands.class);
        Path error = Path.of("diagnostic.log");

        try (var input = new PostgreSqlContainerDumpInputStream(process, error, commands, BusinessErrorCode.DATABASE_DUMP_READ_FAILED)) {
            input.read();
        }

        verify(process).destroyForcibly();
        verify(commands).delete(error);
    }

    @Test
    void shouldRejectFailedDownloadAtEndOfStream() throws Exception {
        Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        var commands = mock(PostgreSqlContainerCommands.class);
        Path error = Path.of("diagnostic.log");
        doThrow(new BusinessException(BusinessErrorType.CONFLICT, BusinessErrorCode.DATABASE_DUMP_READ_FAILED, "failed"))
                .when(commands).check(process, error, BusinessErrorCode.DATABASE_DUMP_READ_FAILED);

        try (var input = new PostgreSqlContainerDumpInputStream(process, error, commands, BusinessErrorCode.DATABASE_DUMP_READ_FAILED)) {
            assertThrows(BusinessException.class, () -> input.transferTo(new ByteArrayOutputStream()));
        }
    }

}

package com.cybermanager.infrastructure.out.adapters.persistences;

import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class PostgreSqlContainerCommandsTest {

    @Test
    void shouldPassPasswordThroughEnvironmentAndCaptureOnlyTextOutput() throws Exception {
        Process process = process(0);
        AtomicReference<List<String>> command = new AtomicReference<>();
        HashMap<String, String> environment = new HashMap<>();
        try (var builders = mockConstruction(ProcessBuilder.class, org.mockito.Mockito.withSettings().defaultAnswer(RETURNS_SELF),
                (builder, context) -> {
                    command.set(((List<?>) context.arguments().getFirst()).stream().map(String.class::cast).toList());
                    when(builder.environment()).thenReturn(environment);
                    prepare(builder, process, "100|1700000000", "");
                })) {
            String output = commands().execute(List.of("stat", "/backups/test.dump"), null, BusinessErrorCode.DATABASE_EXPORT_FAILED);

            assertEquals("100|1700000000", output);
            assertEquals("secret", environment.get("PGPASSWORD"));
            assertEquals(List.of("docker", "exec", "--interactive", "--env", "PGPASSWORD", "test-db", "stat", "/backups/test.dump"), command.get());
            assertFalse(command.get().contains("secret"));
            verify(builders.constructed().getFirst()).start();
        }
    }

    @Test
    void shouldRedactPostgresFailure(CapturedOutput logs) throws Exception {
        Process process = process(1);
        try (var builders = mockConstruction(ProcessBuilder.class, org.mockito.Mockito.withSettings().defaultAnswer(RETURNS_SELF),
                (builder, context) -> prepare(builder, process, "", "connection failed secret"))) {
            var exception = assertThrows(BusinessException.class, () ->
                    commands().execute(List.of("pg_dump"), null, BusinessErrorCode.DATABASE_EXPORT_FAILED));

            assertEquals(BusinessErrorCode.DATABASE_EXPORT_FAILED.name(), exception.code());
            assertTrue(logs.getAll().contains("exitCode=1 connection failed [REDACTED]"));
            assertFalse(logs.getAll().contains("connection failed secret"));
            verify(builders.constructed().getFirst()).start();
        }
    }

    @Test
    void shouldStopTimedOutProcess() throws Exception {
        Process process = process(0);
        when(process.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(false);
        try (var builders = mockConstruction(ProcessBuilder.class, org.mockito.Mockito.withSettings().defaultAnswer(RETURNS_SELF),
                (builder, context) -> prepare(builder, process, "", ""))) {
            assertThrows(BusinessException.class, () ->
                    commands().execute(List.of("pg_dump"), null, BusinessErrorCode.DATABASE_EXPORT_FAILED));

            verify(process).destroyForcibly();
            verify(builders.constructed().getFirst()).start();
        }
    }

    @Test
    void shouldPreserveInterruption() throws Exception {
        Process process = process(0);
        when(process.waitFor(anyLong(), any(TimeUnit.class))).thenThrow(new InterruptedException());
        try (var builders = mockConstruction(ProcessBuilder.class, org.mockito.Mockito.withSettings().defaultAnswer(RETURNS_SELF),
                (builder, context) -> prepare(builder, process, "", ""))) {
            assertThrows(BusinessException.class, () ->
                    commands().execute(List.of("pg_dump"), null, BusinessErrorCode.DATABASE_EXPORT_FAILED));
            assertTrue(Thread.currentThread().isInterrupted());
            verify(builders.constructed().getFirst()).start();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void shouldReportMissingDockerExecutable(CapturedOutput logs) {
        var runner = new PostgreSqlContainerCommands("/missing/docker-client", "test-db", "secret");

        assertThrows(BusinessException.class, () ->
                runner.execute(List.of("pg_dump"), null, BusinessErrorCode.DATABASE_EXPORT_FAILED));
        assertTrue(logs.getAll().contains("/missing/docker-client"));
    }

    @Test
    void shouldRequireContainer() {
        assertThrows(IllegalArgumentException.class, () -> new PostgreSqlContainerCommands("docker", "", "secret"));
    }

    private PostgreSqlContainerCommands commands() {
        return new PostgreSqlContainerCommands("docker", "test-db", "secret");
    }

    private Process process(int exitCode) throws Exception {
        Process process = mock(Process.class);
        when(process.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(process.exitValue()).thenReturn(exitCode);
        when(process.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        return process;
    }

    private void prepare(ProcessBuilder builder, Process process, String output, String diagnostic) throws Exception {
        AtomicReference<File> destination = new AtomicReference<>();
        AtomicReference<File> error = new AtomicReference<>();
        when(builder.redirectOutput(any(File.class))).thenAnswer(invocation -> {
            destination.set(invocation.getArgument(0));
            return builder;
        });
        when(builder.redirectError(any(File.class))).thenAnswer(invocation -> {
            error.set(invocation.getArgument(0));
            return builder;
        });
        when(builder.start()).thenAnswer(invocation -> {
            Files.writeString(destination.get().toPath(), output);
            Files.writeString(error.get().toPath(), diagnostic);
            return process;
        });
    }

}

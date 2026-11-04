package com.cybermanager.infrastructure.out.adapters.persistences;

import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PostgreSqlDatabaseBackupAdapterIntegrationTest {

    @Test
    void shouldCreateArchiveInsideContainerAndReturnMetadata() {
        try (var runners = mockConstruction(PostgreSqlContainerCommands.class, (runner, context) ->
                when(runner.execute(anyList(), isNull(), eq(BusinessErrorCode.DATABASE_EXPORT_FAILED))).thenReturn("120|1700000000"))) {
            var exported = adapter("/backups").exportDatabase();
            ArgumentCaptor<List<String>> command = ArgumentCaptor.captor();
            verify(runners.constructed().getFirst()).execute(command.capture(), isNull(), eq(BusinessErrorCode.DATABASE_EXPORT_FAILED));

            assertEquals(120, exported.size());
            assertTrue(command.getValue().contains("/backups/" + exported.fileName()));
            assertTrue(command.getValue().contains("pg_dump"));
            assertTrue(command.getValue().get(2).contains("--file="));
            assertTrue(command.getValue().contains("--host=localhost"));
            assertTrue(command.getValue().contains("--port=5432"));
            assertFalse(command.getValue().contains("secret"));
        }
    }

    @Test
    void shouldListContainerArchivesSortedByDateAndIgnoreInvalidNames() {
        try (var runners = mockConstruction(PostgreSqlContainerCommands.class, (runner, context) ->
                when(runner.execute(anyList(), isNull(), eq(BusinessErrorCode.DATABASE_DUMP_LIST_FAILED)))
                        .thenReturn("old.dump|10|1000\nnew.dump|20|2000\ninvalid name.dump|30|3000\n"))) {
            var result = adapter("/backups").list();

            assertEquals(List.of("new.dump", "old.dump"), result.stream().map(resource -> resource.fileName()).toList());
            verify(runners.constructed().getFirst()).execute(anyList(), isNull(), eq(BusinessErrorCode.DATABASE_DUMP_LIST_FAILED));
        }
    }

    @Test
    void shouldUploadBytesDirectlyIntoContainer() throws Exception {
        byte[] content = new byte[]{'P', 'G', 'D', 'M', 'P', 0, (byte) 255};
        try (var runners = mockConstruction(PostgreSqlContainerCommands.class, (runner, context) ->
                when(runner.execute(anyList(), any(InputStream.class), eq(BusinessErrorCode.DATABASE_DUMP_UPLOAD_FAILED)))
                        .thenAnswer(invocation -> {
                            assertArrayEquals(content, invocation.<InputStream>getArgument(1).readAllBytes());
                            return "7|1700000000";
                        }))) {
            var result = adapter("/backups").store("manual.dump", new ByteArrayInputStream(content));

            assertEquals("manual.dump", result.fileName());
            assertEquals(7, result.size());
            verify(runners.constructed().getFirst()).execute(anyList(), any(InputStream.class), eq(BusinessErrorCode.DATABASE_DUMP_UPLOAD_FAILED));
        }
    }

    @Test
    void shouldRejectEmptyUploadBeforeStartingDocker() {
        try (var runners = mockConstruction(PostgreSqlContainerCommands.class)) {
            var exception = assertThrows(BusinessException.class, () ->
                    adapter("/backups").store("empty.dump", new ByteArrayInputStream(new byte[0])));

            assertEquals(BusinessErrorCode.DATABASE_DUMP_EMPTY.name(), exception.code());
            verify(runners.constructed().getFirst(), never()).execute(anyList(), any(), any());
        }
    }

    @Test
    void shouldRejectPathTraversalBeforeStartingDocker() {
        try (var runners = mockConstruction(PostgreSqlContainerCommands.class)) {
            var exception = assertThrows(BusinessException.class, () -> adapter("/backups").read("../outside.dump"));

            assertEquals(BusinessErrorType.VALIDATION, exception.type());
            verify(runners.constructed().getFirst(), never()).execute(anyList(), any(), any());
        }
    }

    @Test
    void shouldReportMissingContainerArchive() {
        try (var runners = mockConstruction(PostgreSqlContainerCommands.class, (runner, context) ->
                when(runner.execute(anyList(), isNull(), eq(BusinessErrorCode.DATABASE_DUMP_METADATA_FAILED))).thenReturn("missing"))) {
            var exception = assertThrows(BusinessException.class, () -> adapter("/backups").read("absent.dump"));

            assertEquals(BusinessErrorCode.DATABASE_DUMP_NOT_FOUND.name(), exception.code());
            verify(runners.constructed().getFirst()).execute(anyList(), isNull(), eq(BusinessErrorCode.DATABASE_DUMP_METADATA_FAILED));
        }
    }

    @Test
    void shouldStreamDownloadFromContainer() throws Exception {
        byte[] bytes = new byte[]{0, 1, (byte) 255};
        try (var runners = mockConstruction(PostgreSqlContainerCommands.class, (runner, context) -> {
            when(runner.execute(anyList(), isNull(), eq(BusinessErrorCode.DATABASE_DUMP_METADATA_FAILED))).thenReturn("3|1700000000");
            when(runner.open(anyList(), eq(BusinessErrorCode.DATABASE_DUMP_READ_FAILED))).thenReturn(new ByteArrayInputStream(bytes));
        })) {
            var downloaded = adapter("/backups").read("manual.dump");

            try (var input = downloaded.content()) {
                assertArrayEquals(bytes, input.readAllBytes());
            }
            verify(runners.constructed().getFirst()).open(List.of("cat", "--", "/backups/manual.dump"), BusinessErrorCode.DATABASE_DUMP_READ_FAILED);
        }
    }

    @Test
    void shouldRestoreUsingContainerFilePath() {
        try (var runners = mockConstruction(PostgreSqlContainerCommands.class, (runner, context) ->
                when(runner.execute(anyList(), isNull(), eq(BusinessErrorCode.DATABASE_DUMP_METADATA_FAILED))).thenReturn("100|1700000000"))) {
            adapter("/backups").restore("manual.dump");

            ArgumentCaptor<List<String>> command = ArgumentCaptor.captor();
            verify(runners.constructed().getFirst()).execute(command.capture(), isNull(), eq(BusinessErrorCode.DATABASE_RESTORE_FAILED));
            assertEquals("pg_restore", command.getValue().getFirst());
            assertTrue(command.getValue().contains("/backups/manual.dump"));
            assertTrue(command.getValue().contains("--exit-on-error"));
        }
    }

    @Test
    void shouldRejectWindowsStoragePath() {
        assertThrows(IllegalArgumentException.class, () -> adapter("D:/data/dumps"));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "CM_TEST_BACKUP_CONTAINER", matches = ".+")
    void shouldExportUploadListAndDownloadRealContainerArchives() throws Exception {
        String directory = System.getenv().getOrDefault("CM_DATABASE_DUMP_DIR", "/var/lib/postgresql/dumps")
                + "/verification-" + UUID.randomUUID();
        PostgreSqlDatabaseBackupAdapter adapter = adapter(directory);
        try {
            assertTrue(adapter.list().isEmpty());
            var exported = adapter.exportDatabase();
            assertTrue(exported.size() > 5);

            Process validation = new ProcessBuilder("docker", "exec", System.getenv("CM_TEST_BACKUP_CONTAINER"),
                    "pg_restore", "--list", directory + "/" + exported.fileName())
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.INHERIT).start();
            assertTrue(validation.waitFor(30, TimeUnit.SECONDS));
            assertEquals(0, validation.exitValue());

            byte[] downloaded;
            try (var input = adapter.read(exported.fileName()).content()) {
                downloaded = input.readAllBytes();
            }
            assertEquals(exported.size(), downloaded.length);
            var uploaded = adapter.store("imported.dump", new ByteArrayInputStream(downloaded));
            assertEquals(exported.size(), uploaded.size());
            try (var input = adapter.read("imported.dump").content()) {
                assertArrayEquals(downloaded, input.readAllBytes());
            }
            assertEquals(2, adapter.list().size());
            assertEquals(BusinessErrorCode.DATABASE_DUMP_EMPTY.name(), assertThrows(BusinessException.class, () ->
                    adapter.store("empty.dump", new ByteArrayInputStream(new byte[0]))).code());
            assertEquals(BusinessErrorCode.DATABASE_DUMP_NOT_FOUND.name(), assertThrows(BusinessException.class, () ->
                    adapter.read("missing.dump")).code());
        } finally {
            Process cleanup = new ProcessBuilder("docker", "exec", System.getenv("CM_TEST_BACKUP_CONTAINER"),
                    "rm", "-r", "--", directory).start();
            assertTrue(cleanup.waitFor(30, TimeUnit.SECONDS));
            assertEquals(0, cleanup.exitValue());
        }
    }

    private PostgreSqlDatabaseBackupAdapter adapter(String directory) {
        String container = System.getenv().getOrDefault("CM_TEST_BACKUP_CONTAINER", "test-db");
        return new PostgreSqlDatabaseBackupAdapter(directory, "pg_dump", "pg_restore",
                "jdbc:postgresql://localhost:15432/cybermanager", "cybermanager", "cybermanager", container, "docker");
    }

}

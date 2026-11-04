package com.cybermanager.infrastructure.out.adapters.persistences;

import com.cybermanager.application.ports.database.DatabaseBackupGateway;
import com.cybermanager.application.ports.database.DumpContent;
import com.cybermanager.application.ports.database.DumpResource;
import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class PostgreSqlDatabaseBackupAdapter implements DatabaseBackupGateway {

    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT).withZone(ZoneOffset.UTC);
    private static final String LIST_SCRIPT = """
            set -eu
            mkdir -p -- "$1"
            for file in "$1"/*.dump; do
                [ -f "$file" ] && [ ! -L "$file" ] || continue
                name=${file##*/}
                printf '%s|' "$name"
                stat -c '%s|%Y' -- "$file"
            done
            """;
    private static final String EXPORT_SCRIPT = """
            set -eu
            directory=$1
            destination=$2
            shift 2
            mkdir -p -- "$directory"
            temporary=$(mktemp "$directory/.export-XXXXXX")
            trap 'rm -f -- "$temporary"' EXIT HUP INT TERM
            "$@" --file="$temporary"
            test -s "$temporary"
            mv -- "$temporary" "$destination"
            stat -c '%s|%Y' -- "$destination"
            """;
    private static final String UPLOAD_SCRIPT = """
            set -eu
            mkdir -p -- "$1"
            temporary=$(mktemp "$1/.upload-XXXXXX")
            trap 'rm -f -- "$temporary"' EXIT HUP INT TERM
            cat > "$temporary"
            test -s "$temporary"
            mv -- "$temporary" "$2"
            stat -c '%s|%Y' -- "$2"
            """;
    private static final String METADATA_SCRIPT = """
            set -eu
            if [ -f "$1" ] && [ ! -L "$1" ]; then
                stat -c '%s|%Y' -- "$1"
            else
                printf missing
            fi
            """;

    private final String dumpDirectory;
    private final String pgDumpPath;
    private final String pgRestorePath;
    private final String username;
    private final ConnectionTarget target;
    private final PostgreSqlContainerCommands commands;

    public PostgreSqlDatabaseBackupAdapter(
            @Value("${cybermanager.database-backup.directory}") String dumpDirectory,
            @Value("${cybermanager.database-backup.pg-dump-path}") String pgDumpPath,
            @Value("${cybermanager.database-backup.pg-restore-path}") String pgRestorePath,
            @Value("${spring.datasource.url}") String jdbcUrl,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${cybermanager.database-backup.docker-container}") String dockerContainer,
            @Value("${cybermanager.database-backup.docker-path:docker}") String dockerPath
    ) {
        if (!dumpDirectory.startsWith("/") || dumpDirectory.contains("..") || dumpDirectory.contains("\\")
                || dumpDirectory.contains("\n") || dumpDirectory.contains("|") || "/".equals(dumpDirectory)) {
            throw new IllegalArgumentException("Le repertoire des dumps doit etre un chemin absolu dans le conteneur.");
        }
        this.dumpDirectory = dumpDirectory.replaceAll("/+$", "");
        this.pgDumpPath = pgDumpPath;
        this.pgRestorePath = pgRestorePath;
        this.username = username;
        this.target = ConnectionTarget.from(jdbcUrl);
        this.commands = new PostgreSqlContainerCommands(dockerPath, dockerContainer, password);
    }

    @Override
    public List<DumpResource> list() {
        String output = commands.execute(shell(LIST_SCRIPT, dumpDirectory), null, BusinessErrorCode.DATABASE_DUMP_LIST_FAILED);
        List<DumpResource> resources = new ArrayList<>();
        for (String line : output.lines().toList()) {
            String[] fields = line.split("\\|", 2);
            if (fields.length == 2 && isDumpName(fields[0])) {
                resources.add(toResource(fields[0], fields[1]));
            }
        }
        return resources.stream().sorted(Comparator.comparing(DumpResource::modifiedAt).reversed()).toList();
    }

    @Override
    public DumpResource exportDatabase() {
        String fileName = "cybermanager-" + FILE_DATE.format(Instant.now()) + "-" + UUID.randomUUID() + ".dump";
        List<String> command = shell(EXPORT_SCRIPT, dumpDirectory, resolveDump(fileName));
        command.addAll(databaseCommand(pgDumpPath));
        command.addAll(List.of("--format=custom", "--compress=6", "--no-owner", "--no-privileges", "--schema=cybermanager"));
        String metadata = commands.execute(command, null, BusinessErrorCode.DATABASE_EXPORT_FAILED);
        return toResource(fileName, metadata);
    }

    @Override
    public DumpResource store(String fileName, InputStream content) {
        String destination = resolveDump(fileName);
        PushbackInputStream input = new PushbackInputStream(content);
        try {
            int firstByte = input.read();
            if (firstByte < 0) {
                throw new BusinessException(BusinessErrorType.VALIDATION, BusinessErrorCode.DATABASE_DUMP_EMPTY, "Le dump importe est vide.");
            }
            input.unread(firstByte);
        } catch (IOException exception) {
            throw commands.failure(BusinessErrorCode.DATABASE_DUMP_UPLOAD_FAILED, exception.getMessage());
        }
        String metadata = commands.execute(shell(UPLOAD_SCRIPT, dumpDirectory, destination), input, BusinessErrorCode.DATABASE_DUMP_UPLOAD_FAILED);
        return toResource(fileName, metadata);
    }

    @Override
    public DumpContent read(String fileName) {
        DumpResource resource = existingDump(fileName);
        InputStream input = commands.open(List.of("cat", "--", resolveDump(fileName)), BusinessErrorCode.DATABASE_DUMP_READ_FAILED);
        return new DumpContent(fileName, resource.size(), input);
    }

    @Override
    public void restore(String fileName) {
        existingDump(fileName);
        List<String> command = databaseCommand(pgRestorePath);
        command.addAll(List.of("--clean", "--if-exists", "--exit-on-error", "--no-owner", "--no-privileges",
                "--schema=cybermanager", resolveDump(fileName)));
        commands.execute(command, null, BusinessErrorCode.DATABASE_RESTORE_FAILED);
    }

    private List<String> databaseCommand(String executable) {
        return new ArrayList<>(List.of(executable, "--host=localhost", "--port=5432", "--username=" + username,
                "--dbname=" + target.database(), "--no-password"));
    }

    private List<String> shell(String script, String... arguments) {
        List<String> command = new ArrayList<>(List.of("sh", "-c", script, "database-backup"));
        command.addAll(List.of(arguments));
        return command;
    }

    private DumpResource existingDump(String fileName) {
        String metadata = commands.execute(shell(METADATA_SCRIPT, resolveDump(fileName)), null, BusinessErrorCode.DATABASE_DUMP_METADATA_FAILED);
        if ("missing".equals(metadata.strip())) {
            throw new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.DATABASE_DUMP_NOT_FOUND, "Le dump demande est introuvable.");
        }
        return toResource(fileName, metadata);
    }

    private String resolveDump(String fileName) {
        if (!isDumpName(fileName)) {
            throw new BusinessException(BusinessErrorType.VALIDATION, BusinessErrorCode.DATABASE_DUMP_NAME_INVALID, "Le nom du dump est invalide.");
        }
        return dumpDirectory + "/" + fileName;
    }

    private boolean isDumpName(String fileName) {
        return fileName != null && fileName.matches("[A-Za-z0-9._-]+\\.dump");
    }

    private DumpResource toResource(String fileName, String metadata) {
        DumpResource resource;
        try {
            String[] fields = metadata.strip().split("\\|");
            if (fields.length != 2) {
                throw new IllegalArgumentException("Metadonnees de dump invalides.");
            }
            resource = new DumpResource(fileName, Long.parseLong(fields[0]), Instant.ofEpochSecond(Long.parseLong(fields[1])));
        } catch (IllegalArgumentException exception) {
            throw commands.failure(BusinessErrorCode.DATABASE_DUMP_METADATA_FAILED, exception.getMessage());
        }
        return resource;
    }

}



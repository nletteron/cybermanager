package com.cybermanager.infrastructure.out.adapters.persistences;

import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class PostgreSqlContainerCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSqlContainerCommands.class);
    private static final Duration TIMEOUT = Duration.ofMinutes(10);

    private final String dockerPath;
    private final String container;
    private final String password;

    PostgreSqlContainerCommands(String dockerPath, String container, String password) {
        if (container == null || container.isBlank()) {
            throw new IllegalArgumentException("Le conteneur PostgreSQL des sauvegardes est obligatoire.");
        }
        this.dockerPath = dockerPath;
        this.container = container;
        this.password = password;
    }

    String execute(List<String> arguments, InputStream source, BusinessErrorCode code) {
        Path output = null;
        Path error = null;
        Process process = null;
        FutureTask<Long> upload = null;
        String result;
        try {
            output = Files.createTempFile("postgres-output-", ".log");
            error = Files.createTempFile("postgres-error-", ".log");
            process = builder(arguments).redirectOutput(output.toFile()).redirectError(error.toFile()).start();
            if (source == null) {
                process.getOutputStream().close();
            } else {
                Process uploadingProcess = process;
                upload = new FutureTask<>(() -> {
                    long size;
                    try (var destination = uploadingProcess.getOutputStream()) {
                        size = source.transferTo(destination);
                    }
                    return size;
                });
                Thread.ofVirtual().start(upload);
            }
            check(process, error, code);
            if (upload != null) {
                upload.get(5, TimeUnit.SECONDS);
            }
            result = Files.readString(output);
        } catch (IOException | ExecutionException | TimeoutException exception) {
            throw failure(code, exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw failure(code, "Commande interrompue.");
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            if (upload != null) {
                upload.cancel(true);
            }
            delete(output);
            delete(error);
        }
        return result;
    }

    InputStream open(List<String> arguments, BusinessErrorCode code) {
        Path error = null;
        Process process = null;
        InputStream result;
        try {
            error = Files.createTempFile("postgres-error-", ".log");
            process = builder(arguments).redirectError(error.toFile()).start();
            process.getOutputStream().close();
            result = new PostgreSqlContainerDumpInputStream(process, error, this, code);
        } catch (IOException exception) {
            if (process != null) {
                process.destroyForcibly();
            }
            delete(error);
            throw failure(code, exception.getMessage());
        }
        return result;
    }

    void check(Process process, Path error, BusinessErrorCode code) throws IOException, InterruptedException {
        if (!process.waitFor(TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
            throw failure(code, "Delai depasse.");
        }
        if (process.exitValue() != 0) {
            throw failure(code, "exitCode=" + process.exitValue() + " " + Files.readString(error).strip());
        }
    }

    BusinessException failure(BusinessErrorCode code, String details) {
        String redacted = details;
        if (redacted != null && password != null && !password.isEmpty()) {
            redacted = redacted.replace(password, "[REDACTED]");
        }
        LOGGER.error("PostgreSQL container operation failed code={} container={} docker={} details={}", code, container, dockerPath, redacted);
        return new BusinessException(BusinessErrorType.CONFLICT, code, "L operation sur les sauvegardes PostgreSQL a echoue.");
    }

    void delete(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException exception) {
                LOGGER.warn("Cannot remove PostgreSQL diagnostic file path={}", path);
            }
        }
    }

    private ProcessBuilder builder(List<String> arguments) {
        List<String> command = new ArrayList<>(List.of(dockerPath, "exec", "--interactive", "--env", "PGPASSWORD", container));
        command.addAll(arguments);
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.environment().put("PGPASSWORD", password);
        return builder;
    }

}

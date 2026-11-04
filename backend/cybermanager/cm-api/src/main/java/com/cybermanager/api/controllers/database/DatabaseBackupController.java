package com.cybermanager.api.controllers.database;

import com.cybermanager.api.abstracts.ApiSupport;
import com.cybermanager.api.dtos.database.DatabaseDumpResponse;
import com.cybermanager.api.mappers.database.DatabaseBackupApiMapper;
import com.cybermanager.application.usecases.database.*;
import com.cybermanager.application.views.database.DatabaseDumpView;
import com.cybermanager.domain.queries.database.DownloadDatabaseDumpQuery;
import com.cybermanager.domain.queries.database.ListDatabaseDumpsQuery;
import com.cybermanager.infrastructure.out.adapters.security.users.JwtAccessTokenReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/database-backups")
public class DatabaseBackupController {

    private final ListDatabaseDumpsUseCase listUseCase;
    private final ExportDatabaseUseCase exportUseCase;
    private final UploadDatabaseDumpUseCase uploadUseCase;
    private final DownloadDatabaseDumpUseCase downloadUseCase;
    private final RestoreDatabaseDumpUseCase restoreUseCase;
    private final JwtAccessTokenReader tokenReader;
    private final DatabaseBackupApiMapper databaseBackupApiMapper;

    public DatabaseBackupController(
            ListDatabaseDumpsUseCase listUseCase,
            ExportDatabaseUseCase exportUseCase,
            UploadDatabaseDumpUseCase uploadUseCase,
            DownloadDatabaseDumpUseCase downloadUseCase,
            RestoreDatabaseDumpUseCase restoreUseCase,
            JwtAccessTokenReader tokenReader,
            DatabaseBackupApiMapper databaseBackupApiMapper
    ) {
        this.listUseCase = listUseCase;
        this.exportUseCase = exportUseCase;
        this.uploadUseCase = uploadUseCase;
        this.downloadUseCase = downloadUseCase;
        this.restoreUseCase = restoreUseCase;
        this.tokenReader = tokenReader;
        this.databaseBackupApiMapper = databaseBackupApiMapper;
    }

    @GetMapping
    public List<DatabaseDumpResponse> list(@RequestHeader("Authorization") String authorization) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        return listUseCase.execute(new ListDatabaseDumpsQuery(actor.email(), actor.roles())).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/export")
    public ResponseEntity<DatabaseDumpResponse> export(@RequestHeader("Authorization") String authorization) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var dump = exportUseCase.execute(databaseBackupApiMapper.toExportDatabaseCommand(actor.email(), actor.roles()));
        return ResponseEntity.ok(toResponse(dump));
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DatabaseDumpResponse> upload(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var dump = uploadUseCase.execute(databaseBackupApiMapper.toUploadDatabaseDumpCommand(
                actor.email(),
                actor.roles(),
                file.getOriginalFilename(),
                file.getInputStream()
        ));
        return ResponseEntity.ok(toResponse(dump));
    }

    @GetMapping("/{fileName}/content")
    public ResponseEntity<InputStreamResource> download(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String fileName
    ) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var dump = downloadUseCase.execute(new DownloadDatabaseDumpQuery(actor.email(), actor.roles(), fileName));
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(dump.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(dump.size())
                .body(new InputStreamResource(dump.content()));
    }

    @PostMapping("/{fileName}/restore")
    public ResponseEntity<Void> restore(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String fileName
    ) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        restoreUseCase.execute(databaseBackupApiMapper.toRestoreDatabaseDumpCommand(actor.email(), actor.roles(), fileName));
        return ResponseEntity.noContent().build();
    }

    private DatabaseDumpResponse toResponse(DatabaseDumpView view) {
        return new DatabaseDumpResponse(view.fileName(), view.size(), view.modifiedAt());
    }

}



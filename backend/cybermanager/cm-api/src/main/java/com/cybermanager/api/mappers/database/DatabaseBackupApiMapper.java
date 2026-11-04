package com.cybermanager.api.mappers.database;

import com.cybermanager.domain.commands.database.ExportDatabaseCommand;
import com.cybermanager.domain.commands.database.RestoreDatabaseDumpCommand;
import com.cybermanager.domain.commands.database.UploadDatabaseDumpCommand;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Set;

@Component
public class DatabaseBackupApiMapper {

    public ExportDatabaseCommand toExportDatabaseCommand(String actorEmail, Set<String> actorRoles) {
        return new ExportDatabaseCommand(actorEmail, actorRoles);
    }

    public UploadDatabaseDumpCommand toUploadDatabaseDumpCommand(String actorEmail, Set<String> actorRoles, String fileName, InputStream content) {
        return new UploadDatabaseDumpCommand(actorEmail, actorRoles, fileName, content);
    }

    public RestoreDatabaseDumpCommand toRestoreDatabaseDumpCommand(String actorEmail, Set<String> actorRoles, String fileName) {
        return new RestoreDatabaseDumpCommand(actorEmail, actorRoles, fileName);
    }

}

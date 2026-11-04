package com.cybermanager.domain.commands.database;

import java.io.InputStream;
import java.util.Set;

public record UploadDatabaseDumpCommand(
        String actorEmail,
        Set<String> actorRoles,
        String fileName,
        InputStream content
) {

}

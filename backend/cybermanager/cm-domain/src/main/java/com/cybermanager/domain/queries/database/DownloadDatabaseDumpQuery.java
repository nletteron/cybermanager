package com.cybermanager.domain.queries.database;

import java.util.Set;

public record DownloadDatabaseDumpQuery(String actorEmail, Set<String> actorRoles, String fileName) {

}

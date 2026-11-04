package com.cybermanager.domain.commands.database;

import java.util.Set;

public record RestoreDatabaseDumpCommand(String actorEmail, Set<String> actorRoles, String fileName) {

}

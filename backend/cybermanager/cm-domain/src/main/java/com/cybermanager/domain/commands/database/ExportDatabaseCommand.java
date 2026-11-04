package com.cybermanager.domain.commands.database;

import java.util.Set;

public record ExportDatabaseCommand(String actorEmail, Set<String> actorRoles) {

}

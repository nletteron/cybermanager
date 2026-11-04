package com.cybermanager.domain.queries.database;

import java.util.Set;

public record ListDatabaseDumpsQuery(String actorEmail, Set<String> actorRoles) {

}

package com.cybermanager.domain.queries.users;

import java.util.Set;

public record SearchUsersQuery(
        String actorEmail,
        Set<String> actorRoles,
        String term,
        String status
) {

}


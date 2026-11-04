package com.cybermanager.domain.commands.users;

import java.util.Set;
import java.util.UUID;

public record DeleteUserCommand(
        String actorEmail,
        Set<String> actorRoles,
        UUID userId
) {

}

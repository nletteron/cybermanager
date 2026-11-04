package com.cybermanager.infrastructure.out.adapters.security.users;

import java.util.Set;

public record AuthenticatedActor(String email, Set<String> roles) {

}


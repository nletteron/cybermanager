package com.cybermanager.api.abstracts;

import com.cybermanager.infrastructure.out.adapters.security.users.AuthenticatedActor;
import com.cybermanager.infrastructure.out.adapters.security.users.JwtAccessTokenReader;

public final class ApiSupport {

    private ApiSupport() {
    }

    public static String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing bearer token");
        }
        return authorization.substring(7);
    }

    public static AuthenticatedActor actor(String authorization, JwtAccessTokenReader tokenReader) {
        return tokenReader.read(extractToken(authorization));
    }

}


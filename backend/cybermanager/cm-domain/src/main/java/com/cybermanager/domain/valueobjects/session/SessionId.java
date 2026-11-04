package com.cybermanager.domain.valueobjects.session;

import java.util.UUID;

public record SessionId(UUID value) {

    public static SessionId newId() {
        return new SessionId(UUID.randomUUID());
    }

}


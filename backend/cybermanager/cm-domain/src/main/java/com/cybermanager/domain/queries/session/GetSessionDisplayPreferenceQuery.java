package com.cybermanager.domain.queries.session;

import com.cybermanager.domain.valueobjects.users.EmailAddress;

import java.util.Objects;

public record GetSessionDisplayPreferenceQuery(EmailAddress owner) {

    public GetSessionDisplayPreferenceQuery {
        Objects.requireNonNull(owner, "owner is required");
    }

}

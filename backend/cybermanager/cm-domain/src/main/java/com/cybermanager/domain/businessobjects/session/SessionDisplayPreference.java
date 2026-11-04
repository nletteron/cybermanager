package com.cybermanager.domain.businessobjects.session;

import com.cybermanager.domain.valueobjects.users.EmailAddress;

import java.util.Objects;

public record SessionDisplayPreference(EmailAddress owner, boolean groupEntries) {

    public SessionDisplayPreference {
        Objects.requireNonNull(owner, "owner is required");
    }

}

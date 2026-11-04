package com.cybermanager.domain.commands.session;

import com.cybermanager.domain.valueobjects.users.EmailAddress;

import java.util.Objects;

public record ConfigureSessionDisplayPreferenceCommand(EmailAddress owner, Boolean groupEntries) {

    public ConfigureSessionDisplayPreferenceCommand {
        Objects.requireNonNull(owner, "owner is required");
        if (groupEntries == null) {
            throw new IllegalArgumentException("groupEntries is required");
        }
    }

}

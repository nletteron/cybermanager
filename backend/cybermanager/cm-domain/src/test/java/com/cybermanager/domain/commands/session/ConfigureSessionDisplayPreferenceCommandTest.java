package com.cybermanager.domain.commands.session;

import com.cybermanager.domain.valueobjects.users.EmailAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigureSessionDisplayPreferenceCommandTest {

    @Test
    void shouldAcceptBothChoicesAndRejectMissingValues() {
        var owner = new EmailAddress("alice@example.test");
        assertTrue(new ConfigureSessionDisplayPreferenceCommand(owner, true).groupEntries());
        assertFalse(new ConfigureSessionDisplayPreferenceCommand(owner, false).groupEntries());
        assertThrows(IllegalArgumentException.class, () -> new ConfigureSessionDisplayPreferenceCommand(owner, null));
        assertThrows(NullPointerException.class, () -> new ConfigureSessionDisplayPreferenceCommand(null, true));
    }

}

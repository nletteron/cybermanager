package com.cybermanager.domain.businessobjects.session;

import com.cybermanager.domain.valueobjects.users.EmailAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionDisplayPreferenceTest {

    @Test
    void shouldKeepAnExplicitChoiceAndRequireAnOwner() {
        var owner = new EmailAddress("Alice@example.test");
        assertTrue(new SessionDisplayPreference(owner, true).groupEntries());
        assertFalse(new SessionDisplayPreference(owner, false).groupEntries());
        assertEquals("alice@example.test", new SessionDisplayPreference(owner, true).owner().value());
        assertThrows(NullPointerException.class, () -> new SessionDisplayPreference(null, true));
    }

}

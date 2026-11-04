package com.cybermanager.application.views.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionDisplayPreferenceViewTest {

    @Test
    void shouldDistinguishBothSavedDisplayModes() {
        assertEquals(new SessionDisplayPreferenceView(true), new SessionDisplayPreferenceView(true));
        assertNotEquals(new SessionDisplayPreferenceView(true), new SessionDisplayPreferenceView(false));
        assertFalse(new SessionDisplayPreferenceView(false).groupEntries());
    }

}

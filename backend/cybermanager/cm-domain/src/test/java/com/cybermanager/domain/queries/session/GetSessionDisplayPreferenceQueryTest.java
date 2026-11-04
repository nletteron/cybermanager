package com.cybermanager.domain.queries.session;

import com.cybermanager.domain.valueobjects.users.EmailAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetSessionDisplayPreferenceQueryTest {

    @Test
    void shouldScopeTheQueryToAnExplicitOwner() {
        var owner = new EmailAddress("alice@example.test");
        assertEquals(owner, new GetSessionDisplayPreferenceQuery(owner).owner());
        assertThrows(NullPointerException.class, () -> new GetSessionDisplayPreferenceQuery(null));
    }

}

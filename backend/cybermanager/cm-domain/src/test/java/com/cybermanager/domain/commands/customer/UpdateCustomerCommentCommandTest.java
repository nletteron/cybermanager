package com.cybermanager.domain.commands.customer;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateCustomerCommentCommandTest {

    @Test
    void shouldPreserveCustomerIdentityAndValueSemantics() {
        var id = UUID.randomUUID();
        var value = new UpdateCustomerCommentCommand(id, "Note");
        assertEquals(id, value.customerId());
        assertEquals(new UpdateCustomerCommentCommand(id, "Note"), value);
    }

    @Test
    void shouldRejectMissingCustomerId() {
        assertThrows(NullPointerException.class, () -> new UpdateCustomerCommentCommand(null, "Note"));
    }

}

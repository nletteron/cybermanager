package com.cybermanager.application.views.customer;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CustomerCommentViewTest {

    @Test
    void shouldPreserveCustomerIdentityAndValueSemantics() {
        var id = UUID.randomUUID();
        var value = new CustomerCommentView(id, "Note");
        assertEquals(id, value.customerId());
        assertEquals(new CustomerCommentView(id, "Note"), value);
    }

    @Test
    void shouldRepresentAbsentCommentAsNull() {
        assertNull(new CustomerCommentView(UUID.randomUUID(), null).comment());
    }

}

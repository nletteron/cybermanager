package com.cybermanager.domain.queries.customer;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetCustomerCommentQueryTest {

    @Test
    void shouldPreserveCustomerIdentityAndValueSemantics() {
        var id = UUID.randomUUID();
        var value = new GetCustomerCommentQuery(id);
        assertEquals(id, value.customerId());
        assertEquals(new GetCustomerCommentQuery(id), value);
    }

    @Test
    void shouldRejectMissingCustomerId() {
        assertThrows(NullPointerException.class, () -> new GetCustomerCommentQuery(null));
    }

}

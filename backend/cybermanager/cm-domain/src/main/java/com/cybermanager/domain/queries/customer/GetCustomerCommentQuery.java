package com.cybermanager.domain.queries.customer;

import java.util.Objects;
import java.util.UUID;

public record GetCustomerCommentQuery(UUID customerId) {

    public GetCustomerCommentQuery {
        Objects.requireNonNull(customerId, "customerId is required");
    }

}

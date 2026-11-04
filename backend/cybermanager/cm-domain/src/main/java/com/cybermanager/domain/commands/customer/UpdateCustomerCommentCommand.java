package com.cybermanager.domain.commands.customer;

import java.util.Objects;
import java.util.UUID;

public record UpdateCustomerCommentCommand(UUID customerId, String comment) {

    public UpdateCustomerCommentCommand {
        Objects.requireNonNull(customerId, "customerId is required");
    }

}

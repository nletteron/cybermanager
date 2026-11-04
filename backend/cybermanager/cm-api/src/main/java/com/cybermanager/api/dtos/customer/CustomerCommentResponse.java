package com.cybermanager.api.dtos.customer;

import java.util.UUID;

public record CustomerCommentResponse(UUID customerId, String comment) {

}

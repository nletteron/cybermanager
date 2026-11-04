package com.cybermanager.api.dtos.customer;

import jakarta.validation.constraints.Size;

public record CustomerCommentRequest(@Size(max = 2000) String comment) {

}

package com.cybermanager.domain.valueobjects.customer;

import java.util.UUID;

public record CustomerId(UUID value) {

    public static CustomerId newId() {
        return new CustomerId(UUID.randomUUID());
    }

}


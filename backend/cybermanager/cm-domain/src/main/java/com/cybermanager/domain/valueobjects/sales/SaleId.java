package com.cybermanager.domain.valueobjects.sales;

import java.util.UUID;

public record SaleId(UUID value) {

    public static SaleId newId() {
        return new SaleId(UUID.randomUUID());
    }

}


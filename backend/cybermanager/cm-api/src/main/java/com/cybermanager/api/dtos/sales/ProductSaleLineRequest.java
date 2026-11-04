package com.cybermanager.api.dtos.sales;

import java.util.UUID;

public record ProductSaleLineRequest(UUID productId, int quantity) {

}

package com.cybermanager.domain.commands.sales;

import java.util.UUID;

public record ProductSaleLineCommand(UUID productId, int quantity) {

}

package com.cybermanager.api.dtos.customer;

import java.util.UUID;

public record ConversionResponse(CustomerResponse customer, UUID saleId, int deductedMinutes) {

}

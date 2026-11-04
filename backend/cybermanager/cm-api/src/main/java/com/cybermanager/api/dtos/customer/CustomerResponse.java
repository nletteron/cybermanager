package com.cybermanager.api.dtos.customer;

import java.math.BigDecimal;
import java.util.UUID;

public record CustomerResponse(UUID customerId, String name, String type, String status, int remainingMinutes, BigDecimal openDebtAmount) {

}

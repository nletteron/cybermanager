package com.cybermanager.api.dtos.sales;

import java.util.List;
import java.util.UUID;

public record CreateInvoiceRequest(UUID customerId, String customerName, List<ManualInvoiceLineRequest> lines) {

}

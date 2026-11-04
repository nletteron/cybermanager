package com.cybermanager.domain.commands.sales;

import java.util.List;
import java.util.UUID;

public record CreateManualInvoiceCommand(
        String actorEmail,
        UUID customerId,
        String customerName,
        List<ManualInvoiceLineCommand> lines
) {

}

package com.cybermanager.api.mappers.sales;

import com.cybermanager.api.dtos.sales.*;
import com.cybermanager.domain.commands.sales.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class SalesApiMapper {

    public CreateManualInvoiceCommand toCreateManualInvoiceCommand(String actorEmail, CreateInvoiceRequest request) {
        return new CreateManualInvoiceCommand(
                actorEmail,
                request.customerId(),
                request.customerName(),
                request.lines().stream()
                        .map(line -> new ManualInvoiceLineCommand(line.label(), line.quantity(), line.unitPrice()))
                        .toList()
        );
    }

    public CancelInvoiceCommand toCancelInvoiceCommand(UUID invoiceId) {
        return new CancelInvoiceCommand(invoiceId);
    }

    public CreateProductSaleCommand toCreateProductSaleCommand(String actorEmail, Set<String> actorRoles, ProductSaleRequest request) {
        return new CreateProductSaleCommand(
                actorEmail,
                actorRoles,
                request.customerId(),
                request.sessionId(),
                request.lines().stream()
                        .map(line -> new ProductSaleLineCommand(line.productId(), line.quantity()))
                        .toList(),
                request.createDebt()
        );
    }

    public CreateSubscriptionSaleCommand toCreateSubscriptionSaleCommand(String actorEmail, Set<String> actorRoles, SubscriptionSaleRequest request) {
        int quantity = request.quantity() == null || request.quantity() < 1 ? 1 : request.quantity();
        return new CreateSubscriptionSaleCommand(actorEmail, actorRoles, request.customerId(), request.sessionId(), request.subscriptionOfferId(), quantity, request.createDebt());
    }

    public CreateConnectionTimeSaleCommand toCreateConnectionTimeSaleCommand(String actorEmail, Set<String> actorRoles, ConnectionTimeSaleRequest request) {
        return new CreateConnectionTimeSaleCommand(actorEmail, actorRoles, request.customerId(), request.sessionId(), request.minutes(), request.createDebt());
    }

    public ConfigureConnectionPricingCommand toConfigureConnectionPricingCommand(String actorEmail, Set<String> actorRoles, PricingRequest request) {
        return new ConfigureConnectionPricingCommand(
                actorEmail,
                actorRoles,
                request.tiers().stream()
                        .map(tier -> new PricingTierCommand(tier.hours(), tier.minutes(), tier.price()))
                        .toList()
        );
    }

}

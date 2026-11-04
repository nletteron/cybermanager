package com.cybermanager.domain.businessobjects.sales;

import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.sales.SaleId;
import com.cybermanager.domain.valueobjects.sales.SaleLine;
import com.cybermanager.domain.valueobjects.sales.SaleType;
import com.cybermanager.domain.valueobjects.shared.Money;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Sale(
        SaleId id,
        CustomerId customerId,
        UUID sessionId,
        SaleType type,
        LocalDateTime soldAt,
        List<SaleLine> lines,
        Money totalAmount
) {

    public Sale {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(customerId, "customerId is required");
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(soldAt, "soldAt is required");
        lines = List.copyOf(lines);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("At least one line is required");
        }
        Objects.requireNonNull(totalAmount, "totalAmount is required");
    }

    public static Sale create(CustomerId customerId, SaleType type, LocalDateTime soldAt, List<SaleLine> lines, Money totalAmount) {
        return new Sale(SaleId.newId(), customerId, null, type, soldAt, lines, totalAmount);
    }

    public static Sale create(CustomerId customerId, UUID sessionId, SaleType type, LocalDateTime soldAt, List<SaleLine> lines, Money totalAmount) {
        return new Sale(SaleId.newId(), customerId, sessionId, type, soldAt, lines, totalAmount);
    }

}


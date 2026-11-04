package com.cybermanager.domain.businessobjects.customer;

import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.customer.DebtId;
import com.cybermanager.domain.valueobjects.customer.DebtStatus;
import com.cybermanager.domain.valueobjects.sales.SaleId;
import com.cybermanager.domain.valueobjects.shared.Money;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record DebtRecord(
        DebtId id,
        CustomerId customerId,
        String label,
        String comment,
        Money amount,
        DebtStatus status,
        LocalDateTime createdAt,
        LocalDateTime settledAt,
        UUID saleId
) {

    public DebtRecord {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(customerId, "customerId is required");
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Debt label is required");
        }
        comment = comment == null ? "" : comment.trim();
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
    }

    public DebtRecord(DebtId id, CustomerId customerId, String label, String comment, Money amount,
                      DebtStatus status, LocalDateTime createdAt, LocalDateTime settledAt) {
        this(id, customerId, label, comment, amount, status, createdAt, settledAt, null);
    }

    public static DebtRecord create(CustomerId customerId, String label, Money amount, LocalDateTime createdAt) {
        return new DebtRecord(DebtId.newId(), customerId, label.trim(), "", amount, DebtStatus.OPEN, createdAt, null);
    }

    public static DebtRecord createForSale(CustomerId customerId, String label, Money amount, LocalDateTime createdAt, UUID saleId) {
        return new DebtRecord(DebtId.newId(), customerId, label.trim(), "", amount, DebtStatus.OPEN, createdAt, null,
                Objects.requireNonNull(saleId, "saleId is required"));
    }

    public boolean isForSale(SaleId id) {
        return saleId != null && saleId.equals(id.value());
    }

    public DebtRecord settle(LocalDateTime settledAt, String comment) {
        return new DebtRecord(id, customerId, label, comment, amount, DebtStatus.SETTLED, createdAt, settledAt, saleId);
    }

}

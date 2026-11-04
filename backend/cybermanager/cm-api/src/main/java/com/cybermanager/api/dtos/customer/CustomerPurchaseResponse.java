package com.cybermanager.api.dtos.customer;

import com.cybermanager.api.dtos.sales.SaleLineResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CustomerPurchaseResponse(UUID saleId, UUID sessionId, String type, String label, String debtLabel, LocalDateTime soldAt, BigDecimal totalAmount, boolean openDebt, UUID debtId, List<SaleLineResponse> lines) {

    public CustomerPurchaseResponse {
        lines = List.copyOf(lines);
    }

    public CustomerPurchaseResponse(UUID saleId, UUID sessionId, String type, String label, String debtLabel, LocalDateTime soldAt, BigDecimal totalAmount, boolean openDebt, UUID debtId) {
        this(saleId, sessionId, type, label, debtLabel, soldAt, totalAmount, openDebt, debtId, List.of());
    }

    public CustomerPurchaseResponse(UUID saleId, UUID sessionId, String type, String label, String debtLabel, LocalDateTime soldAt, BigDecimal totalAmount, boolean openDebt) {
        this(saleId, sessionId, type, label, debtLabel, soldAt, totalAmount, openDebt, null);
    }

}

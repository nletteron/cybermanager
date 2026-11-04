package com.cybermanager.application.views.customer;

import com.cybermanager.application.views.sales.SaleLineView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CustomerSaleView(UUID saleId, UUID sessionId, String type, String label, String debtLabel, LocalDateTime soldAt, BigDecimal totalAmount, boolean openDebt, UUID debtId, List<SaleLineView> lines) {

    public CustomerSaleView {
        lines = List.copyOf(lines);
    }

    public CustomerSaleView(UUID saleId, UUID sessionId, String type, String label, String debtLabel, LocalDateTime soldAt, BigDecimal totalAmount, boolean openDebt, UUID debtId) {
        this(saleId, sessionId, type, label, debtLabel, soldAt, totalAmount, openDebt, debtId, List.of());
    }

    public CustomerSaleView(UUID saleId, UUID sessionId, String type, String label, String debtLabel, LocalDateTime soldAt, BigDecimal totalAmount, boolean openDebt) {
        this(saleId, sessionId, type, label, debtLabel, soldAt, totalAmount, openDebt, null);
    }

}

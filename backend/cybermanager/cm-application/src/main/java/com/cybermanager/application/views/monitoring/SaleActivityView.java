package com.cybermanager.application.views.monitoring;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaleActivityView(String label, int quantity, BigDecimal totalPrice, boolean debt, LocalDateTime soldAt) {

}

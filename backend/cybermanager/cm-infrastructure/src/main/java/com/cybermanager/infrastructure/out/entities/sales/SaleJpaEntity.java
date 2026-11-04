package com.cybermanager.infrastructure.out.entities.sales;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "cm_sales",
        indexes = {
                @Index(name = "idx_cm_sales_customer_sold_at", columnList = "customerId, soldAt"),
                @Index(name = "idx_cm_sales_session_id", columnList = "sessionId")
        }
)
public class SaleJpaEntity {

    @Id
    public UUID id;
    public UUID customerId;
    public UUID sessionId;
    public String type;
    public LocalDateTime soldAt;
    public BigDecimal totalAmount;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<SaleLineJpaEntity> lines = new ArrayList<>();

}


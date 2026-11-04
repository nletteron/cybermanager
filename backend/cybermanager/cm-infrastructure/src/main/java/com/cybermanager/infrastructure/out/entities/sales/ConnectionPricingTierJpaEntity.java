package com.cybermanager.infrastructure.out.entities.sales;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cm_connection_pricing_tiers")
public class ConnectionPricingTierJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grid_id", nullable = false)
    public ConnectionPricingGridJpaEntity grid;

    @Column(name = "duration_minutes", nullable = false)
    public int durationMinutes;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal price;

    @Column
    public LocalDateTime createdAt;

    @Column
    public LocalDateTime updatedAt;

}

package com.cybermanager.infrastructure.out.entities.sales;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cm_connection_pricing_grids")
public class ConnectionPricingGridJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public boolean active;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    @Column(nullable = false)
    public LocalDateTime updatedAt;

}

package com.cybermanager.infrastructure.out.entities.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cm_product_categories")
public class CategoryJpaEntity {

    @Id
    public UUID id;

    @Column(nullable = false, unique = true)
    public String name;

    @Column(nullable = false, length = 1000)
    public String description;

    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 2)
    public BigDecimal vatRate;

}

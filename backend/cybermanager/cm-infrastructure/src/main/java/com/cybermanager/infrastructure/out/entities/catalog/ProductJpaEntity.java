package com.cybermanager.infrastructure.out.entities.catalog;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cm_products")
public class ProductJpaEntity {

    @Id
    public UUID id;
    public String name;
    public BigDecimal price;
    @ManyToOne
    @JoinColumn(name = "category_id")
    public CategoryJpaEntity categoryReference;
    public String category;
    public String description;
    public String status;

}


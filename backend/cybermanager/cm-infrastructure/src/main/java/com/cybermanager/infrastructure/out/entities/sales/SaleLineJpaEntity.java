package com.cybermanager.infrastructure.out.entities.sales;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cm_sale_lines")
public class SaleLineJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String label;
    public int quantity;
    public BigDecimal unitPrice;
    public BigDecimal totalPrice;

    @ManyToOne
    @JoinColumn(name = "sale_id")
    public SaleJpaEntity sale;

}


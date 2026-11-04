package com.cybermanager.infrastructure.out.entities.sales;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cm_invoice_lines")
public class InvoiceLineJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "invoice_id")
    public InvoiceJpaEntity invoice;

    public String label;
    public int quantity;
    public BigDecimal unitPrice;
    public BigDecimal totalPrice;

}

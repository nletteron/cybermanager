package com.cybermanager.infrastructure.out.entities.customer;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "cm_customers",
        indexes = {
                @Index(name = "idx_cm_customers_name", columnList = "name"),
                @Index(name = "idx_cm_customers_type_name", columnList = "type, name")
        }
)
public class CustomerJpaEntity {

    @Id
    public UUID id;
    public String name;
    public String type;
    public String status;
    public int remainingMinutes;
    @Column(length = 2000)
    public String comment;

}


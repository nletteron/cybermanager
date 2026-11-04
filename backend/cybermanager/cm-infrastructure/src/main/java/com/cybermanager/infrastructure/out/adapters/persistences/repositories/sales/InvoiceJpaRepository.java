package com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales;

import com.cybermanager.infrastructure.out.entities.sales.InvoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceJpaEntity, UUID>, JpaSpecificationExecutor<InvoiceJpaEntity> {

}

package com.cybermanager.infrastructure.out.adapters.persistences.sales;

import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.ports.sales.SaleRepository;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.sales.SaleId;
import com.cybermanager.domain.valueobjects.sales.SaleLine;
import com.cybermanager.domain.valueobjects.sales.SaleType;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales.SaleJpaRepository;
import com.cybermanager.infrastructure.out.entities.sales.SaleJpaEntity;
import com.cybermanager.infrastructure.out.entities.sales.SaleLineJpaEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class SaleRepositoryAdapter implements SaleRepository {

    private final SaleJpaRepository repository;

    public SaleRepositoryAdapter(SaleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Sale save(Sale sale) {
        SaleJpaEntity entity = new SaleJpaEntity();
        entity.id = sale.id().value();
        entity.customerId = sale.customerId().value();
        entity.sessionId = sale.sessionId();
        entity.type = sale.type().name();
        entity.soldAt = sale.soldAt();
        entity.totalAmount = sale.totalAmount().amount();
        entity.lines = sale.lines().stream()
                .map(line -> toEntity(line, entity))
                .toList();

        return toDomain(repository.save(entity));
    }

    private SaleLineJpaEntity toEntity(SaleLine line, SaleJpaEntity sale) {
        SaleLineJpaEntity entity = new SaleLineJpaEntity();
        entity.sale = sale;
        entity.label = line.label();
        entity.quantity = line.quantity();
        entity.unitPrice = line.unitPrice().amount();
        entity.totalPrice = line.totalPrice().amount();

        return entity;
    }

    @Override
    public Optional<Sale> findById(SaleId saleId) {
        return repository.findById(saleId.value()).map(this::toDomain);
    }

    @Override
    public void deleteById(SaleId saleId) {
        repository.deleteById(saleId.value());
    }

    @Override
    public void deleteByCustomerIds(List<CustomerId> customerIds) {
        repository.deleteByCustomerIdIn(customerIds.stream().map(CustomerId::value).toList());
    }

    @Override
    public List<Sale> findByDay(LocalDate date) {
        return repository.findBySoldAtBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Sale> findByCustomerId(CustomerId customerId) {
        return repository.findByCustomerIdOrderBySoldAtDesc(customerId.value()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Sale> findByCustomerIds(List<CustomerId> customerIds) {
        return repository.findByCustomerIdInOrderBySoldAtDesc(customerIds.stream().map(CustomerId::value).toList()).stream().map(this::toDomain).toList();
    }

    private Sale toDomain(SaleJpaEntity entity) {
        return new Sale(
                new SaleId(entity.id),
                new CustomerId(entity.customerId),
                entity.sessionId,
                SaleType.valueOf(entity.type),
                entity.soldAt,
                entity.lines.stream().map(line -> new SaleLine(line.label, line.quantity, new Money(line.unitPrice), new Money(line.totalPrice))).toList(),
                new Money(entity.totalAmount)
        );
    }

}



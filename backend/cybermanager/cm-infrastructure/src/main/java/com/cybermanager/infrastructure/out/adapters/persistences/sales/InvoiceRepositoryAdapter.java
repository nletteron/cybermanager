package com.cybermanager.infrastructure.out.adapters.persistences.sales;

import com.cybermanager.domain.businessobjects.sales.Invoice;
import com.cybermanager.domain.ports.sales.InvoiceRepository;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.sales.InvoiceLine;
import com.cybermanager.domain.valueobjects.sales.InvoiceStatus;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales.InvoiceJpaRepository;
import com.cybermanager.infrastructure.out.entities.sales.InvoiceJpaEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class InvoiceRepositoryAdapter implements InvoiceRepository {

    private final InvoiceJpaRepository repository;

    public InvoiceRepositoryAdapter(InvoiceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Invoice save(Invoice invoice) {
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.id = invoice.id();
        entity.saleId = invoice.saleId();
        entity.customerId = invoice.customerId() == null ? null : invoice.customerId().value();
        entity.invoiceNumber = invoice.invoiceNumber();
        entity.customerName = invoice.customerName();
        entity.issuedAt = invoice.issuedAt();
        entity.status = invoice.status().name();
        entity.totalAmount = invoice.totalAmount().amount();
        entity.lines = invoice.lines().stream()
                .map(line -> toEntity(line, entity))
                .toList();

        return toDomain(repository.save(entity));
    }

    private com.cybermanager.infrastructure.out.entities.sales.InvoiceLineJpaEntity toEntity(InvoiceLine line, InvoiceJpaEntity invoice) {
        var entity = new com.cybermanager.infrastructure.out.entities.sales.InvoiceLineJpaEntity();
        entity.invoice = invoice;
        entity.label = line.label();
        entity.quantity = line.quantity();
        entity.unitPrice = line.unitPrice().amount();
        entity.totalPrice = line.totalPrice().amount();

        return entity;
    }

    @Override
    public List<Invoice> search(String invoiceNumber, String customerName, InvoiceStatus status, LocalDate startDate, LocalDate endDate) {
        Specification<InvoiceJpaEntity> specification = Specification.allOf(List.of());
        if (invoiceNumber != null && !invoiceNumber.isBlank()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("invoiceNumber")), "%" + invoiceNumber.toLowerCase() + "%"));
        }
        if (customerName != null && !customerName.isBlank()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("customerName")), "%" + customerName.toLowerCase() + "%"));
        }
        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status.name()));
        }
        if (startDate != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("issuedAt"), startDate.atStartOfDay()));
        }
        if (endDate != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("issuedAt"), endDate.plusDays(1).atStartOfDay()));
        }
        return repository.findAll(specification).stream()
                .sorted(Comparator.comparing((InvoiceJpaEntity entity) -> entity.issuedAt).reversed())
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Invoice> findById(UUID invoiceId) {
        return repository.findById(invoiceId).map(this::toDomain);
    }

    private Invoice toDomain(InvoiceJpaEntity entity) {
        return new Invoice(
                entity.id,
                entity.saleId,
                entity.invoiceNumber,
                entity.customerId == null ? null : new CustomerId(entity.customerId),
                entity.customerName,
                entity.issuedAt,
                InvoiceStatus.valueOf(entity.status),
                new Money(entity.totalAmount),
                entity.lines.stream()
                        .map(line -> new InvoiceLine(line.label, line.quantity, new Money(line.unitPrice), new Money(line.totalPrice)))
                        .toList()
        );
    }

}



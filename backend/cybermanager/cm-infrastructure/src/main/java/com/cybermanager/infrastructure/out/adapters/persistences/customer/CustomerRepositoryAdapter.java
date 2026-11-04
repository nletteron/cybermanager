package com.cybermanager.infrastructure.out.adapters.persistences.customer;

import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.customer.CustomerStatus;
import com.cybermanager.domain.valueobjects.customer.CustomerType;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.customer.CustomerJpaRepository;
import com.cybermanager.infrastructure.out.entities.customer.CustomerJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository repository;

    public CustomerRepositoryAdapter(CustomerJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.id = customer.id().value();
        entity.name = customer.name();
        entity.type = customer.type().name();
        entity.status = customer.status().name();
        entity.remainingMinutes = customer.remainingMinutes();
        entity.comment = customer.comment();
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        return repository.findById(customerId.value()).map(this::toDomain);
    }

    @Override
    public void deleteByIds(List<CustomerId> customerIds) {
        repository.deleteAllByIdInBatch(customerIds.stream().map(CustomerId::value).toList());
    }

    @Override
    public List<Customer> search(String term, CustomerType type) {
        String search = term == null ? "" : term.trim();
        List<CustomerJpaEntity> entities;

        if (type != null && !search.isBlank()) {
            entities = repository.findByTypeAndNameContainingIgnoreCaseOrderByNameAsc(type.name(), search);
        } else if (type != null) {
            entities = repository.findByTypeOrderByNameAsc(type.name());
        } else if (!search.isBlank()) {
            entities = repository.findByNameContainingIgnoreCaseOrderByNameAsc(search);
        } else {
            entities = repository.findAllByOrderByNameAsc();
        }

        return entities.stream().map(this::toDomain).toList();
    }

    private Customer toDomain(CustomerJpaEntity entity) {
        return new Customer(new CustomerId(entity.id), entity.name, CustomerType.valueOf(entity.type), CustomerStatus.valueOf(entity.status), entity.remainingMinutes, entity.comment);
    }

}




package com.cybermanager.domain.ports.sales;

import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.sales.SaleId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SaleRepository {

    Sale save(Sale sale);

    Optional<Sale> findById(SaleId saleId);

    void deleteById(SaleId saleId);

    void deleteByCustomerIds(List<CustomerId> customerIds);

    List<Sale> findByDay(LocalDate date);

    List<Sale> findByCustomerId(CustomerId customerId);

    List<Sale> findByCustomerIds(List<CustomerId> customerIds);

}

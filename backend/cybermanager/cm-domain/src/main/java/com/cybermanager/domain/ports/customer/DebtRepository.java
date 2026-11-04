package com.cybermanager.domain.ports.customer;

import com.cybermanager.domain.businessobjects.customer.DebtRecord;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.customer.DebtId;

import java.util.List;
import java.util.Optional;

public interface DebtRepository {

    DebtRecord save(DebtRecord debtRecord);

    Optional<DebtRecord> findById(DebtId debtId);

    void deleteById(DebtId debtId);

    void deleteByCustomerIds(List<CustomerId> customerIds);

    List<DebtRecord> findByCustomerId(CustomerId customerId);

    List<DebtRecord> findByCustomerIds(List<CustomerId> customerIds);

    List<DebtRecord> findOpenDebts();

}

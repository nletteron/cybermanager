package com.cybermanager.application.usecases.customer;

import com.cybermanager.application.views.customer.CustomerCommentView;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.exceptions.DomainException;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.queries.customer.GetCustomerCommentQuery;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetCustomerCommentUseCase {

    private final CustomerRepository customers;

    public GetCustomerCommentUseCase(CustomerRepository customers) {
        this.customers = customers;
    }

    public CustomerCommentView execute(GetCustomerCommentQuery query) {
        var customer = customers.findById(new CustomerId(query.customerId()))
                .orElseThrow(() -> new DomainException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));
        return new CustomerCommentView(customer.id().value(), customer.comment());
    }

}

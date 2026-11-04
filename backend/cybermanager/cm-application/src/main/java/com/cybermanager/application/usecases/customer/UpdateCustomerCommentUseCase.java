package com.cybermanager.application.usecases.customer;

import com.cybermanager.application.views.customer.CustomerCommentView;
import com.cybermanager.domain.commands.customer.UpdateCustomerCommentCommand;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.exceptions.DomainException;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateCustomerCommentUseCase {

    private final CustomerRepository customers;

    public UpdateCustomerCommentUseCase(CustomerRepository customers) {
        this.customers = customers;
    }

    public CustomerCommentView execute(UpdateCustomerCommentCommand command) {
        var customer = customers.findById(new CustomerId(command.customerId()))
                .orElseThrow(() -> new DomainException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));
        var saved = customers.save(customer.updateComment(command.comment()));
        return new CustomerCommentView(saved.id().value(), saved.comment());
    }

}

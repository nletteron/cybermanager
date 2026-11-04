package com.cybermanager.domain.businessobjects.customer;

import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.exceptions.DomainException;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.customer.CustomerStatus;
import com.cybermanager.domain.valueobjects.customer.CustomerType;

import java.util.Objects;

public record Customer(
        CustomerId id,
        String name,
        CustomerType type,
        CustomerStatus status,
        int remainingMinutes,
        String comment
) {

    public Customer {
        Objects.requireNonNull(id, "id is required");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(status, "status is required");
        if (remainingMinutes < 0) {
            throw new IllegalArgumentException("Remaining minutes cannot be negative");
        }
        comment = comment == null || comment.isBlank() ? null : comment.strip();
        if (comment != null && comment.length() > 2000) {
            throw new DomainException(BusinessErrorType.VALIDATION, BusinessErrorCode.CUSTOMER_COMMENT_TOO_LONG, "Customer comment cannot exceed 2000 characters");
        }
    }

    public Customer(CustomerId id, String name, CustomerType type, CustomerStatus status, int remainingMinutes) {
        this(id, name, type, status, remainingMinutes, null);
    }

    public static Customer createWalkIn(String name) {
        return new Customer(CustomerId.newId(), name.trim(), CustomerType.WALK_IN, CustomerStatus.ACTIVE, 0);
    }

    public Customer updateName(String name) {
        return new Customer(id, name, type, status, remainingMinutes, comment);
    }

    public Customer updateComment(String comment) {
        return new Customer(id, name, type, status, remainingMinutes, comment);
    }

    public Customer convertToSubscriber(int creditedMinutes) {
        return new Customer(id, name, CustomerType.SUBSCRIBER, status, creditedMinutes, comment);
    }

    public Customer deductMinutes(int consumedMinutes) {
        return new Customer(id, name, type, status, Math.max(0, remainingMinutes - consumedMinutes), comment);
    }

    public Customer addSubscriptionMinutes(int creditedMinutes) {
        int nextMinutes = remainingMinutes + Math.max(0, creditedMinutes);
        return new Customer(id, name, CustomerType.SUBSCRIBER, status, nextMinutes, comment);
    }

    public Customer removeSubscriptionMinutes(int minutesToRemove) {
        int nextMinutes = Math.max(0, remainingMinutes - Math.max(0, minutesToRemove));
        return new Customer(id, name, CustomerType.SUBSCRIBER, status, nextMinutes, comment);
    }

    public Customer withRemainingMinutes(int nextRemainingMinutes) {
        return new Customer(id, name, type, status, Math.max(0, nextRemainingMinutes), comment);
    }

}


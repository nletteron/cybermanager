package com.cybermanager.api.mappers.customer;

import com.cybermanager.api.dtos.customer.*;
import com.cybermanager.domain.commands.customer.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class CustomerApiMapper {

    public CreateCustomerCommand toCreateCustomerCommand(String actorEmail, Set<String> actorRoles, CustomerRequest request) {
        return new CreateCustomerCommand(actorEmail, actorRoles, request.name(), request.type(), request.subscriptionOfferId());
    }

    public UpdateCustomerCommand toUpdateCustomerCommand(String actorEmail, Set<String> actorRoles, UUID customerId, CustomerRequest request) {
        return new UpdateCustomerCommand(actorEmail, actorRoles, customerId, request.name());
    }

    public ConvertCustomerToSubscriberCommand toConvertCustomerToSubscriberCommand(
            String actorEmail,
            Set<String> actorRoles,
            UUID customerId,
            ConvertToSubscriberRequest request
    ) {
        return new ConvertCustomerToSubscriberCommand(actorEmail, actorRoles, customerId, request.subscriptionOfferId(), request.deductCurrentSession(), request.sessionId());
    }

    public SettleDebtCommand toSettleDebtCommand(String actorEmail, Set<String> actorRoles, UUID debtId, SettleDebtRequest request) {
        return new SettleDebtCommand(actorEmail, actorRoles, debtId, request == null ? null : request.comment());
    }

    public ReattachDebtToSessionCommand toReattachDebtToSessionCommand(
            String actorEmail,
            Set<String> actorRoles,
            UUID debtId,
            ReattachDebtToSessionRequest request
    ) {
        return new ReattachDebtToSessionCommand(actorEmail, actorRoles, debtId, request.sessionId());
    }

    public CreateDebtFromSaleCommand toCreateDebtFromSaleCommand(String actorEmail, Set<String> actorRoles, UUID saleId) {
        return new CreateDebtFromSaleCommand(actorEmail, actorRoles, saleId);
    }

    public ArchiveCustomersCommand toArchiveCustomersCommand(String actorEmail, Set<String> actorRoles, ArchiveCustomersRequest request) {
        return new ArchiveCustomersCommand(actorEmail, actorRoles, request.startDate(), request.endDate(), request.type(), request.format());
    }

}

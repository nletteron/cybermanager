package com.cybermanager.api.mappers.session;

import com.cybermanager.api.dtos.session.PaySessionRequest;
import com.cybermanager.api.dtos.session.StartSessionRequest;
import com.cybermanager.domain.commands.session.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SessionApiMapper {

    public StartSessionCommand toStartSessionCommand(StartSessionRequest request) {
        return new StartSessionCommand(request.customerId(), request.customerName());
    }

    public StopSessionCommand toStopSessionCommand(UUID sessionId) {
        return new StopSessionCommand(sessionId);
    }

    public PaySessionCommand toPaySessionCommand(UUID sessionId, PaySessionRequest request) {
        return new PaySessionCommand(
                sessionId,
                request == null ? null : request.amountPaid(),
                request == null || request.subscriptionOfferIds() == null ? List.of() : request.subscriptionOfferIds(),
                request != null && request.createSubscriptionDebt()
        );
    }

    public PaySessionAndCreateInvoiceCommand toPaySessionAndCreateInvoiceCommand(UUID sessionId, PaySessionRequest request) {
        return new PaySessionAndCreateInvoiceCommand(
                sessionId,
                request == null ? null : request.amountPaid(),
                request == null || request.subscriptionOfferIds() == null ? List.of() : request.subscriptionOfferIds(),
                request != null && request.createSubscriptionDebt()
        );
    }

    public PauseSessionCommand toPauseSessionCommand(UUID sessionId) {
        return new PauseSessionCommand(sessionId);
    }

    public ResumeSessionCommand toResumeSessionCommand(UUID sessionId) {
        return new ResumeSessionCommand(sessionId);
    }

    public RestartSessionsDayCommand toRestartSessionsDayCommand() {
        return new RestartSessionsDayCommand();
    }

}

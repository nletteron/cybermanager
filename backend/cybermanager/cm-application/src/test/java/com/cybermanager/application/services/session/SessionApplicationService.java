package com.cybermanager.application.services.session;

import com.cybermanager.application.services.sales.InvoiceNumberGenerator;
import com.cybermanager.application.services.sales.InvoicePdfRenderer;
import com.cybermanager.application.usecases.session.*;
import com.cybermanager.application.views.session.CurrentSessionsView;
import com.cybermanager.application.views.session.SessionView;
import com.cybermanager.domain.commands.session.*;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.ports.customer.DebtRepository;
import com.cybermanager.domain.ports.sales.ConnectionPricingRepository;
import com.cybermanager.domain.ports.sales.InvoiceRepository;
import com.cybermanager.domain.ports.sales.SaleRepository;
import com.cybermanager.domain.ports.session.CafeSessionRepository;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;

class SessionApplicationService {

    private final StartSessionUseCase startSessionUseCase;
    private final StopSessionUseCase stopSessionUseCase;
    private final PaySessionUseCase paySessionUseCase;
    private final GetCurrentSessionsUseCase getCurrentSessionsUseCase;
    private final RestartSessionsDayUseCase restartSessionsDayUseCase;
    private final PauseSessionUseCase pauseSessionUseCase;
    private final ResumeSessionUseCase resumeSessionUseCase;

    SessionApplicationService(
            CafeSessionRepository sessionRepository,
            CustomerRepository customerRepository,
            ConnectionPricingRepository pricingRepository,
            DebtRepository debtRepository,
            SaleRepository saleRepository,
            SubscriptionOfferRepository subscriptionOfferRepository,
            InvoiceRepository invoiceRepository,
            InvoicePdfRenderer invoicePdfRenderer,
            InvoiceNumberGenerator invoiceNumberGenerator
    ) {
        this.startSessionUseCase = new StartSessionUseCase(sessionRepository, customerRepository, pricingRepository, debtRepository, saleRepository, subscriptionOfferRepository, invoiceRepository, invoicePdfRenderer, invoiceNumberGenerator);
        this.stopSessionUseCase = new StopSessionUseCase(sessionRepository, customerRepository, pricingRepository, debtRepository, saleRepository, subscriptionOfferRepository, invoiceRepository, invoicePdfRenderer, invoiceNumberGenerator);
        this.paySessionUseCase = new PaySessionUseCase(sessionRepository, customerRepository, pricingRepository, debtRepository, saleRepository, subscriptionOfferRepository, invoiceRepository, invoicePdfRenderer, invoiceNumberGenerator);
        this.getCurrentSessionsUseCase = new GetCurrentSessionsUseCase(sessionRepository, customerRepository, pricingRepository, debtRepository, saleRepository, subscriptionOfferRepository, invoiceRepository, invoicePdfRenderer, invoiceNumberGenerator);
        this.restartSessionsDayUseCase = new RestartSessionsDayUseCase(sessionRepository, customerRepository, pricingRepository, debtRepository, saleRepository, subscriptionOfferRepository, invoiceRepository, invoicePdfRenderer, invoiceNumberGenerator);
        this.pauseSessionUseCase = new PauseSessionUseCase(sessionRepository, customerRepository, pricingRepository, debtRepository, saleRepository, subscriptionOfferRepository, invoiceRepository, invoicePdfRenderer, invoiceNumberGenerator);
        this.resumeSessionUseCase = new ResumeSessionUseCase(sessionRepository, customerRepository, pricingRepository, debtRepository, saleRepository, subscriptionOfferRepository, invoiceRepository, invoicePdfRenderer, invoiceNumberGenerator);
    }

    SessionView execute(StartSessionCommand command) {
        return startSessionUseCase.execute(command);
    }

    SessionView execute(StopSessionCommand command) {
        return stopSessionUseCase.execute(command);
    }

    SessionView execute(PaySessionCommand command) {
        return paySessionUseCase.execute(command);
    }

    CurrentSessionsView execute() {
        return getCurrentSessionsUseCase.execute();
    }

    int execute(RestartSessionsDayCommand command) {
        return restartSessionsDayUseCase.execute(command);
    }

    SessionView execute(PauseSessionCommand command) {
        return pauseSessionUseCase.execute(command);
    }

    SessionView execute(ResumeSessionCommand command) {
        return resumeSessionUseCase.execute(command);
    }

}

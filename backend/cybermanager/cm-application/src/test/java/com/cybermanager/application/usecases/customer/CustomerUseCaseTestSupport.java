package com.cybermanager.application.usecases.customer;

import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.ports.customer.DebtRepository;
import com.cybermanager.domain.ports.customer.GeneratedArchiveFileRepository;
import com.cybermanager.domain.ports.sales.SaleRepository;
import com.cybermanager.domain.ports.session.CafeSessionRepository;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;

import static org.mockito.Mockito.mock;

final class CustomerUseCaseTestSupport {

    final CustomerRepository customerRepository = mock(CustomerRepository.class);
    final SaleRepository saleRepository = mock(SaleRepository.class);
    final DebtRepository debtRepository = mock(DebtRepository.class);
    final CafeSessionRepository sessionRepository = mock(CafeSessionRepository.class);
    final SubscriptionOfferRepository subscriptionOfferRepository = mock(SubscriptionOfferRepository.class);
    final GeneratedArchiveFileRepository generatedArchiveFileRepository = mock(GeneratedArchiveFileRepository.class);

    private CustomerUseCaseTestSupport() {
    }

    static CustomerUseCaseTestSupport create() {
        return new CustomerUseCaseTestSupport();
    }

    SearchCustomerArchiveCandidatesUseCase searchArchiveCandidatesUseCase() {
        return new SearchCustomerArchiveCandidatesUseCase(
                customerRepository,
                subscriptionOfferRepository,
                saleRepository,
                debtRepository,
                generatedArchiveFileRepository,
                sessionRepository
        );
    }

    ArchiveCustomersUseCase archiveCustomersUseCase() {
        return new ArchiveCustomersUseCase(
                customerRepository,
                subscriptionOfferRepository,
                saleRepository,
                debtRepository,
                generatedArchiveFileRepository,
                sessionRepository
        );
    }

    ExportCustomerDebtReportUseCase exportCustomerDebtReportUseCase() {
        return new ExportCustomerDebtReportUseCase(
                customerRepository,
                subscriptionOfferRepository,
                saleRepository,
                debtRepository,
                generatedArchiveFileRepository,
                sessionRepository
        );
    }

}

package com.cybermanager.application.usecases.sales;

import com.cybermanager.domain.ports.catalog.ProductRepository;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.ports.customer.DebtRepository;
import com.cybermanager.domain.ports.sales.ConnectionPricingRepository;
import com.cybermanager.domain.ports.sales.SaleRepository;
import com.cybermanager.domain.ports.session.CafeSessionRepository;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;

import static org.mockito.Mockito.mock;

final class SalesUseCaseTestSupport {

    final SaleRepository saleRepository = mock(SaleRepository.class);
    final ConnectionPricingRepository pricingRepository = mock(ConnectionPricingRepository.class);
    final ProductRepository productRepository = mock(ProductRepository.class);
    final SubscriptionOfferRepository offerRepository = mock(SubscriptionOfferRepository.class);
    final CustomerRepository customerRepository = mock(CustomerRepository.class);
    final DebtRepository debtRepository = mock(DebtRepository.class);
    final CafeSessionRepository sessionRepository = mock(CafeSessionRepository.class);

    private SalesUseCaseTestSupport() {
    }

    static SalesUseCaseTestSupport create() {
        return new SalesUseCaseTestSupport();
    }

    CreateConnectionTimeSaleUseCase createConnectionTimeSaleUseCase() {
        return new CreateConnectionTimeSaleUseCase(
                saleRepository,
                pricingRepository,
                productRepository,
                offerRepository,
                customerRepository,
                debtRepository,
                sessionRepository
        );
    }

    ConfigureConnectionPricingUseCase configureConnectionPricingUseCase() {
        return new ConfigureConnectionPricingUseCase(
                saleRepository,
                pricingRepository,
                productRepository,
                offerRepository,
                customerRepository,
                debtRepository,
                sessionRepository
        );
    }

    DeleteSubscriptionSaleUseCase deleteSubscriptionSaleUseCase() {
        return new DeleteSubscriptionSaleUseCase(
                saleRepository,
                pricingRepository,
                productRepository,
                offerRepository,
                customerRepository,
                debtRepository,
                sessionRepository
        );
    }

    CreateSubscriptionSaleUseCase createSubscriptionSaleUseCase() {
        return new CreateSubscriptionSaleUseCase(
                saleRepository,
                pricingRepository,
                productRepository,
                offerRepository,
                customerRepository,
                debtRepository,
                sessionRepository
        );
    }

}

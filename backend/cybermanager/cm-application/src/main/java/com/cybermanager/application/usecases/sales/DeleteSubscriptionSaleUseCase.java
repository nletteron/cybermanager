package com.cybermanager.application.usecases.sales;

import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.application.services.shared.DateTimeLabelFormatter;
import com.cybermanager.application.views.sales.ConnectionPricingView;
import com.cybermanager.application.views.sales.PricingTierView;
import com.cybermanager.application.views.sales.SaleLineView;
import com.cybermanager.application.views.sales.SaleView;
import com.cybermanager.domain.businessobjects.customer.DebtRecord;
import com.cybermanager.domain.businessobjects.sales.ConnectionPricingRule;
import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.commands.sales.ProductSaleLineCommand;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.ports.catalog.ProductRepository;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.ports.customer.DebtRepository;
import com.cybermanager.domain.ports.sales.ConnectionPricingRepository;
import com.cybermanager.domain.ports.sales.SaleRepository;
import com.cybermanager.domain.ports.session.CafeSessionRepository;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;
import com.cybermanager.domain.valueobjects.catalog.ProductId;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.customer.DebtStatus;
import com.cybermanager.domain.valueobjects.sales.ConnectionPricingTier;
import com.cybermanager.domain.valueobjects.sales.SaleId;
import com.cybermanager.domain.valueobjects.sales.SaleLine;
import com.cybermanager.domain.valueobjects.sales.SaleType;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class DeleteSubscriptionSaleUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteSubscriptionSaleUseCase.class);

    private final SaleRepository saleRepository;
    private final ConnectionPricingRepository pricingRepository;
    private final ProductRepository productRepository;
    private final SubscriptionOfferRepository subscriptionOfferRepository;
    private final CustomerRepository customerRepository;
    private final DebtRepository debtRepository;
    private final CafeSessionRepository sessionRepository;

    public DeleteSubscriptionSaleUseCase(SaleRepository saleRepository, ConnectionPricingRepository pricingRepository, ProductRepository productRepository, SubscriptionOfferRepository subscriptionOfferRepository, CustomerRepository customerRepository, DebtRepository debtRepository, CafeSessionRepository sessionRepository) {

        this.saleRepository = saleRepository;
        this.pricingRepository = pricingRepository;
        this.productRepository = productRepository;
        this.subscriptionOfferRepository = subscriptionOfferRepository;
        this.customerRepository = customerRepository;
        this.debtRepository = debtRepository;
        this.sessionRepository = sessionRepository;

    }


    public void execute(UUID saleId) {
        var sale = saleRepository.findById(new SaleId(saleId))
                .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.SALE_NOT_FOUND, "Sale not found"));
        if (sale.type() != SaleType.SUBSCRIPTION) {
            throw new BusinessException(BusinessErrorType.VALIDATION, BusinessErrorCode.SALE_NOT_SUBSCRIPTION, "Only subscription sales can be deleted");
        }
        if (sale.sessionId() == null) {
            throw new BusinessException(BusinessErrorType.VALIDATION, BusinessErrorCode.SALE_SESSION_REQUIRED, "Sale is not linked to a session");
        }

        var session = sessionRepository.findById(new com.cybermanager.domain.valueobjects.session.SessionId(sale.sessionId()))
                .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.SESSION_NOT_FOUND, "Session not found"));
        if (session.endedAt() == null || session.paid()) {
            throw new BusinessException(BusinessErrorType.CONFLICT, BusinessErrorCode.SALE_DELETE_FORBIDDEN, "Subscription sale can only be deleted before payment");
        }

        var customer = customerRepository.findById(sale.customerId())
                .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));
        int includedMinutes = sale.lines().stream()
                .mapToInt(line -> findIncludedMinutes(line.label()) * line.quantity())
                .sum();
        customerRepository.save(customer.removeSubscriptionMinutes(includedMinutes));

        debtRepository.findByCustomerId(customer.id()).stream()
                .filter(debt -> debt.status() == DebtStatus.OPEN)
                .filter(debt -> debt.isForSale(sale.id()))
                .findFirst()
                .ifPresent(debt -> debtRepository.deleteById(debt.id()));

        saleRepository.deleteById(sale.id());
        LOGGER.info("Subscription sale deleted saleId={} customerId={} includedMinutes={}", sale.id().value(), customer.id().value(), includedMinutes);
    }


    private SaleLine toProductSaleLine(ProductSaleLineCommand line) {
        var product = productRepository.findById(new ProductId(line.productId()))
                .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.PRODUCT_NOT_FOUND, "Product not found"));
        var total = new Money(product.price().amount().multiply(BigDecimal.valueOf(line.quantity())));

        return new SaleLine(product.name(), line.quantity(), product.price(), total);
    }

private void createDebtIfRequested(java.util.UUID customerId, boolean createDebt, String label, Money amount) {
        if (createDebt && amount.amount().signum() > 0) {
            debtRepository.save(DebtRecord.create(new CustomerId(customerId), label, amount, LocalDateTime.now()));
            LOGGER.info("Debt created from sale customerId={} amount={} label={}", customerId, amount.amount(), label);
        }
    }


    private void validateSessionLink(java.util.UUID customerId, java.util.UUID sessionId) {
        if (sessionId != null) {
            var session = sessionRepository.findById(new com.cybermanager.domain.valueobjects.session.SessionId(sessionId))
                    .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.SESSION_NOT_FOUND, "Session not found"));
            if (!session.customerId().equals(new CustomerId(customerId))) {
                throw new BusinessException(BusinessErrorType.VALIDATION, BusinessErrorCode.SESSION_CUSTOMER_MISMATCH, "Session does not belong to customer");
            }
        }
    }


    private String debtLabelForSale(Sale sale) {
        return switch (sale.type()) {
            case PRODUCTS -> sale.lines().stream()
                    .map(line -> line.quantity() + " x " + line.label())
                    .reduce((left, right) -> left + ", " + right)
                    .map(label -> label + " du " + DateTimeLabelFormatter.format(sale.soldAt()))
                    .orElse("Vente produits du " + DateTimeLabelFormatter.format(sale.soldAt()));
            case SUBSCRIPTION -> "Vente abonnement du " + DateTimeLabelFormatter.format(sale.soldAt());
            case CONNECTION_TIME -> "Vente temps du " + DateTimeLabelFormatter.format(sale.soldAt());
        };
    }


    private SaleView toView(Sale sale) {
        return new SaleView(
                sale.id().value(),
                sale.customerId().value(),
                sale.type().name(),
                sale.soldAt(),
                sale.lines().stream().map(line -> new SaleLineView(line.label(), line.quantity(), line.unitPrice().amount(), line.totalPrice().amount())).toList(),
                sale.totalAmount().amount()
        );
    }


    private ConnectionPricingView toPricingView(ConnectionPricingRule rule) {
        return new ConnectionPricingView(rule.tiers().stream()
                .map(this::toPricingTierView)
                .toList());
    }


    private PricingTierView toPricingTierView(ConnectionPricingTier tier) {
        return new PricingTierView(
                tier.id(),
                tier.durationMinutes() / 60,
                tier.durationMinutes() % 60,
                tier.durationMinutes(),
                tier.price().amount(),
                tier.createdAt(),
                tier.updatedAt()
        );
    }


    private int findIncludedMinutes(String offerLabel) {
        return subscriptionOfferRepository.search(offerLabel, SubscriptionOfferStatus.ACTIVE).stream()
                .filter(offer -> offer.name().equalsIgnoreCase(offerLabel))
                .findFirst()
                .or(() -> subscriptionOfferRepository.search(offerLabel, SubscriptionOfferStatus.INACTIVE).stream()
                        .filter(offer -> offer.name().equalsIgnoreCase(offerLabel))
                        .findFirst())
                .map(offer -> offer.includedMinutes())
                .orElse(0);
    }

}

package com.cybermanager.application.usecases.session;

import com.cybermanager.application.services.sales.InvoiceNumberGenerator;
import com.cybermanager.application.services.sales.InvoicePdfRenderer;
import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.application.services.shared.DateTimeLabelFormatter;
import com.cybermanager.application.views.session.SessionView;
import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.customer.DebtRecord;
import com.cybermanager.domain.businessobjects.sales.ConnectionPricingRule;
import com.cybermanager.domain.businessobjects.sales.Invoice;
import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.businessobjects.session.CafeSession;
import com.cybermanager.domain.commands.session.RestartSessionsDayCommand;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.ports.customer.DebtRepository;
import com.cybermanager.domain.ports.sales.ConnectionPricingRepository;
import com.cybermanager.domain.ports.sales.InvoiceRepository;
import com.cybermanager.domain.ports.sales.SaleRepository;
import com.cybermanager.domain.ports.session.CafeSessionRepository;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;
import com.cybermanager.domain.valueobjects.customer.CustomerType;
import com.cybermanager.domain.valueobjects.customer.DebtStatus;
import com.cybermanager.domain.valueobjects.sales.InvoiceLine;
import com.cybermanager.domain.valueobjects.sales.InvoiceStatus;
import com.cybermanager.domain.valueobjects.sales.SaleLine;
import com.cybermanager.domain.valueobjects.sales.SaleType;
import com.cybermanager.domain.valueobjects.session.SessionId;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferId;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RestartSessionsDayUseCase {

    private static final String DEFAULT_WORKSTATION = "SESSION";
    private static final Logger LOGGER = LoggerFactory.getLogger(RestartSessionsDayUseCase.class);

    private final CafeSessionRepository sessionRepository;
    private final CustomerRepository customerRepository;
    private final ConnectionPricingRepository connectionPricingRepository;
    private final DebtRepository debtRepository;
    private final SaleRepository saleRepository;
    private final SubscriptionOfferRepository subscriptionOfferRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoicePdfRenderer invoicePdfRenderer;
    private final InvoiceNumberGenerator invoiceNumberGenerator;

    public RestartSessionsDayUseCase(
            CafeSessionRepository sessionRepository,
            CustomerRepository customerRepository,
            ConnectionPricingRepository connectionPricingRepository,
            DebtRepository debtRepository,
            SaleRepository saleRepository,
            SubscriptionOfferRepository subscriptionOfferRepository,
            InvoiceRepository invoiceRepository,
            InvoicePdfRenderer invoicePdfRenderer,
            InvoiceNumberGenerator invoiceNumberGenerator
    ) {

        this.sessionRepository = sessionRepository;
        this.customerRepository = customerRepository;
        this.connectionPricingRepository = connectionPricingRepository;
        this.debtRepository = debtRepository;
        this.saleRepository = saleRepository;
        this.subscriptionOfferRepository = subscriptionOfferRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoicePdfRenderer = invoicePdfRenderer;
        this.invoiceNumberGenerator = invoiceNumberGenerator;

    }


    public int execute(RestartSessionsDayCommand command) {
        var sessionsToClose = sessionRepository.findAll().stream()
                .filter(session -> !session.paid())
                .toList();

        for (var session : sessionsToClose) {
            var customer = customerRepository.findById(session.customerId())
                    .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));
            var closedSession = closeSessionForDayEnd(session, customer);
            if (closedSession.calculatedPrice().amount().signum() > 0) {
                debtRepository.save(DebtRecord.create(
                        customer.id(),
                        sessionDebtLabel(customer.type(), closedSession.startedAt()),
                        closedSession.calculatedPrice(),
                        LocalDateTime.now()
                ));
            }
            createDebtsForSessionSales(closedSession, customer);
            sessionRepository.save(closedSession.markPaid());
        }
        LOGGER.info("Sessions day restart executed archivedSessionCount={}", sessionsToClose.size());
        return sessionsToClose.size();
    }


    private String sessionDebtLabel(CustomerType customerType, LocalDateTime startedAt) {
        return customerType == CustomerType.SUBSCRIBER
                ? "Depassement abonnement du " + DateTimeLabelFormatter.format(startedAt)
                : "Session du " + DateTimeLabelFormatter.format(startedAt);
    }


    private Customer applyPaymentSubscriptionOffers(CafeSession session, Customer customer, List<java.util.UUID> subscriptionOfferIds, boolean createSubscriptionDebt) {
        Customer updatedCustomer = customer;

        if (subscriptionOfferIds != null && !subscriptionOfferIds.isEmpty() && subscriptionOfferRepository == null) {
            throw new BusinessException(BusinessErrorType.VALIDATION, BusinessErrorCode.SUBSCRIPTION_OFFERS_UNAVAILABLE, "Subscription offers are unavailable");
        }

        if (subscriptionOfferIds != null && !subscriptionOfferIds.isEmpty()) {
            for (var subscriptionOfferId : subscriptionOfferIds) {
                var offer = subscriptionOfferRepository.findById(new SubscriptionOfferId(subscriptionOfferId))
                        .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.SUBSCRIPTION_OFFER_NOT_FOUND, "Subscription offer not found"));
                updatedCustomer = customerRepository.save(updatedCustomer.addSubscriptionMinutes(offer.includedMinutes()));
                var offerSale = saleRepository.save(Sale.create(
                        updatedCustomer.id(),
                        session.id().value(),
                        SaleType.SUBSCRIPTION,
                        LocalDateTime.now(),
                        List.of(new SaleLine(offer.name(), 1, offer.price(), offer.price())),
                        offer.price()
                ));
                if (createSubscriptionDebt) {
                    debtRepository.save(DebtRecord.createForSale(
                            updatedCustomer.id(),
                            debtLabelForSale(offerSale),
                            offerSale.totalAmount(),
                            LocalDateTime.now(), offerSale.id().value()));
                }
            }
        }

        return updatedCustomer;
    }


    private CafeSession closeSessionForDayEnd(CafeSession session, Customer customer) {
        CafeSession result = session;

        if (session.endedAt() == null) {
            LocalDateTime stoppedAt = LocalDateTime.now();
            Money price = Money.of("0");
            if (customer.type() == CustomerType.WALK_IN) {
                ConnectionPricingRule pricing = connectionPricingRepository.getCurrentRule();
                int minutes = session.consumedMinutesUntil(stoppedAt);
                price = pricing.priceForMinutes(minutes);
            } else {
                int consumedMinutes = session.consumedMinutesUntil(stoppedAt);
                int availableMinutes = customer.remainingMinutes();
                int overtimeMinutes = Math.max(0, consumedMinutes - availableMinutes);
                if (overtimeMinutes > 0) {
                    ConnectionPricingRule pricing = connectionPricingRepository.getCurrentRule();
                    price = pricing.priceForMinutes(overtimeMinutes);
                }
                customerRepository.save(customer.deductMinutes(consumedMinutes));
            }
            result = session.stop(stoppedAt, price, false);
        }

        return result;
    }


    private void createDebtsForSessionSales(CafeSession session, Customer customer) {
        LocalDateTime referenceTime = session.endedAt() == null ? LocalDateTime.now() : session.endedAt();
        var existingOpenDebts = debtRepository.findByCustomerId(customer.id()).stream()
                .filter(debt -> debt.status() == DebtStatus.OPEN)
                .toList();
        saleRepository.findByCustomerId(customer.id()).stream()
                .filter(sale -> session.id().value().equals(sale.sessionId())
                        || (sale.sessionId() == null
                        && !sale.soldAt().isBefore(session.startedAt())
                        && !sale.soldAt().isAfter(referenceTime)))
                .filter(sale -> existingOpenDebts.stream().noneMatch(debt ->
                        debt.isForSale(sale.id())))
                .forEach(sale -> debtRepository.save(DebtRecord.createForSale(
                        sale.customerId(),
                        debtLabelForSale(sale),
                        sale.totalAmount(),
                        LocalDateTime.now(), sale.id().value())));
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


    private SessionView toView(CafeSession session) {
        var customer = customerRepository.findById(session.customerId())
                .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));
        return toView(session, customer);
    }


    private SessionView toView(CafeSession session, Customer customer) {
        LocalDateTime referenceTime = session.endedAt() == null ? LocalDateTime.now() : session.endedAt();
        int consumedSeconds = session.endedAt() == null ? session.consumedSecondsUntil(referenceTime) : session.consumedSeconds();
        int consumedMinutes = session.endedAt() == null ? session.consumedMinutesUntil(referenceTime) : Math.max(0, (session.consumedSeconds() + 59) / 60);
        var snapshot = computeFinancialSnapshot(session, customer);
        LOGGER.debug(
                "Session financial snapshot sessionId={} customerId={} customerType={} consumedMinutes={} purchases={} payablePurchases={} openDebt={} totalDue={} totalPaid={}",
                session.id().value(),
                customer.id().value(),
                customer.type(),
                consumedMinutes,
                snapshot.purchasesAmount().amount(),
                snapshot.payablePurchasesAmount(),
                snapshot.openDebtAmount().amount(),
                snapshot.totalAmountDue().amount(),
                snapshot.totalPaidAmount().amount()
        );
        return new SessionView(
                session.id().value(),
                customer.id().value(),
                customer.name(),
                customer.type().name(),
                customer.remainingMinutes(),
                computeDisplayRemainingMinutes(session, customer),
                session.workstation(),
                session.startedAt(),
                session.endedAt(),
                session.paused(),
                session.paid(),
                consumedSeconds,
                consumedMinutes,
                snapshot.connectionAmount().amount(),
                snapshot.purchasesAmount().amount(),
                snapshot.openDebtAmount().amount(),
                snapshot.totalAmountDue().amount(),
                snapshot.totalPaidAmount().amount()
        );
    }


    private int computeDisplayRemainingMinutes(CafeSession session, Customer customer) {
        int result = customer.remainingMinutes();

        if (customer.type() == CustomerType.SUBSCRIBER && session.endedAt() != null && !session.paid()) {
            int compensatedMinutes = saleRepository.findByCustomerId(customer.id()).stream()
                    .filter(sale -> sale.type() == SaleType.SUBSCRIPTION)
                    .filter(sale -> session.id().value().equals(sale.sessionId()))
                    .filter(sale -> !sale.soldAt().isBefore(session.endedAt()))
                    .mapToInt(this::subscriptionIncludedMinutes)
                    .sum();
            int retroConsumedMinutes = Math.min(compensatedMinutes, Math.max(0, (session.consumedSeconds() + 59) / 60));
            result = Math.max(0, customer.remainingMinutes() - retroConsumedMinutes);
        }

        return result;
    }


    private int subscriptionIncludedMinutes(Sale sale) {
        return sale.lines().stream()
                .mapToInt(line -> findIncludedMinutes(line.label()))
                .sum();
    }


    private int findIncludedMinutes(String offerLabel) {
        var activeMatch = subscriptionOfferRepository == null ? List.<com.cybermanager.domain.businessobjects.subscription.SubscriptionOffer>of()
                : subscriptionOfferRepository.search(offerLabel, SubscriptionOfferStatus.ACTIVE);
        var activeExact = activeMatch.stream()
                .filter(offer -> offer.name().equalsIgnoreCase(offerLabel))
                .findFirst();
        int result;

        if (activeExact.isPresent()) {
            result = activeExact.get().includedMinutes();
        } else if (subscriptionOfferRepository == null) {
            result = 0;
        } else {
            result = subscriptionOfferRepository.search(offerLabel, SubscriptionOfferStatus.INACTIVE).stream()
                    .filter(offer -> offer.name().equalsIgnoreCase(offerLabel))
                    .findFirst()
                    .map(offer -> offer.includedMinutes())
                    .orElse(0);
        }

        return result;
    }


    private SessionFinancialSnapshot computeFinancialSnapshot(CafeSession session, Customer customer) {
        LocalDateTime referenceTime = session.endedAt() == null ? LocalDateTime.now() : session.endedAt();
        int consumedMinutes = session.endedAt() == null ? session.consumedMinutesUntil(referenceTime) : Math.max(0, (session.consumedSeconds() + 59) / 60);
        Money connectionAmount = session.calculatedPrice();
        if (customer.type() == CustomerType.WALK_IN) {
            if (session.endedAt() == null) {
                connectionAmount = connectionPricingRepository.getCurrentRule().priceForMinutes(consumedMinutes);
            }
        } else {
            int overtimeMinutes = Math.max(0, consumedMinutes - customer.remainingMinutes());
            Money recomputedAmount = overtimeMinutes > 0
                    ? connectionPricingRepository.getCurrentRule().priceForMinutes(overtimeMinutes)
                    : Money.of("0");
            connectionAmount = session.endedAt() == null
                    ? recomputedAmount
                    : new Money(session.calculatedPrice().amount().min(recomputedAmount.amount()));
        }

        var salesOfSession = saleRepository.findByCustomerId(customer.id()).stream()
                .filter(sale -> session.id().value().equals(sale.sessionId())
                        || (sale.sessionId() == null && !sale.soldAt().isBefore(session.startedAt()) && !sale.soldAt().isAfter(referenceTime)))
                .toList();
        Money purchasesAmount = salesOfSession.stream()
                .map(Sale::totalAmount)
                .reduce(Money.of("0"), Money::add);
        var allOpenDebts = debtRepository.findByCustomerId(customer.id()).stream()
                .filter(debt -> debt.status() == DebtStatus.OPEN)
                .toList();
        Money openDebtAmount = allOpenDebts.stream()
                .map(DebtRecord::amount)
                .reduce(Money.of("0"), Money::add);
        BigDecimal payablePurchasesAmount = salesOfSession.stream()
                .filter(sale -> allOpenDebts.stream().noneMatch(debt ->
                        debt.isForSale(sale.id())))
                .map(sale -> sale.totalAmount().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal dueConnectionAmount = session.paid() ? BigDecimal.ZERO : connectionAmount.amount();
        BigDecimal paidConnectionAmount = session.paid() ? connectionAmount.amount() : BigDecimal.ZERO;
        return new SessionFinancialSnapshot(
                connectionAmount,
                purchasesAmount,
                openDebtAmount,
                payablePurchasesAmount,
                new Money(dueConnectionAmount.add(payablePurchasesAmount)),
                new Money(payablePurchasesAmount.add(paidConnectionAmount))
        );
    }


    private PaidSessionResult settleSession(java.util.UUID sessionId, BigDecimal amountPaid, List<java.util.UUID> subscriptionOfferIds, boolean createSubscriptionDebt) {
        var session = sessionRepository.findById(new SessionId(sessionId))
                .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.SESSION_NOT_FOUND, "Session not found"));
        if (session.endedAt() == null) {
            LOGGER.warn("Pay rejected because session is still active sessionId={}", sessionId);
            throw new BusinessException(BusinessErrorType.CONFLICT, BusinessErrorCode.SESSION_NOT_STOPPED, "Session must be stopped before payment");
        }
        if (session.paid()) {
            LOGGER.warn("Pay rejected because session is already paid sessionId={}", sessionId);
            throw new BusinessException(BusinessErrorType.CONFLICT, BusinessErrorCode.SESSION_ALREADY_PAID, "Session is already paid");
        }
        var customer = customerRepository.findById(session.customerId())
                .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));

        customer = applyPaymentSubscriptionOffers(session, customer, subscriptionOfferIds, createSubscriptionDebt);

        SessionFinancialSnapshot snapshot = computeFinancialSnapshot(session, customer);
        customer = deductRetroactiveSubscriptionMinutes(session, customer, subscriptionOfferIds);
        BigDecimal paid = amountPaid == null ? BigDecimal.ZERO : amountPaid.max(BigDecimal.ZERO);
        BigDecimal expected = snapshot.totalAmountDue().amount();
        BigDecimal remainingAmount = expected.subtract(paid).max(BigDecimal.ZERO);

        if (remainingAmount.signum() > 0) {
            debtRepository.save(DebtRecord.create(
                    customer.id(),
                    sessionDebtLabel(customer.type(), session.startedAt()),
                    new Money(remainingAmount),
                    LocalDateTime.now()
            ));
            LOGGER.info("Remaining session amount moved to debt sessionId={} customerId={} remainingAmount={}", session.id().value(), customer.id().value(), remainingAmount);
        }

        var paidSession = sessionRepository.save(session.stop(session.endedAt(), snapshot.connectionAmount(), true));
        LOGGER.info("Session marked as paid sessionId={} customerId={} expectedAmount={} paidAmount={}", paidSession.id().value(), customer.id().value(), expected, paid);
        return new PaidSessionResult(customer, paidSession, snapshot);
    }


    private Customer deductRetroactiveSubscriptionMinutes(CafeSession session, Customer customer, List<java.util.UUID> subscriptionOfferIds) {
        Customer updatedCustomer = customer;
        if (customer.type() == CustomerType.SUBSCRIBER && subscriptionOfferIds != null && !subscriptionOfferIds.isEmpty()) {
            int consumedMinutes = Math.max(0, (session.consumedSeconds() + 59) / 60);
            updatedCustomer = customer.deductMinutes(consumedMinutes);
            customerRepository.save(updatedCustomer);
        }
        return updatedCustomer;
    }


    private Invoice createInvoiceForPaidSession(PaidSessionResult result) {
        List<InvoiceLine> lines = new java.util.ArrayList<>();
        if (result.snapshot().connectionAmount().amount().signum() > 0) {
            lines.add(new InvoiceLine(
                    "Connexion session du " + DateTimeLabelFormatter.format(result.session().startedAt()),
                    1,
                    result.snapshot().connectionAmount(),
                    result.snapshot().connectionAmount()
            ));
        }

        for (Sale sale : payableSalesOfSession(result.session(), result.customer())) {
            for (SaleLine line : sale.lines()) {
                lines.add(new InvoiceLine(line.label(), line.quantity(), line.unitPrice(), line.totalPrice()));
            }
        }

        if (lines.isEmpty()) {
            lines.add(new InvoiceLine("Reglement de session", 1, Money.of("0"), Money.of("0")));
        }

        Money total = lines.stream().map(InvoiceLine::totalPrice).reduce(Money.of("0"), Money::add);
        return invoiceRepository.save(new Invoice(
                java.util.UUID.randomUUID(),
                null,
                invoiceNumberGenerator.next(),
                result.customer().id(),
                result.customer().name(),
                LocalDateTime.now(),
                InvoiceStatus.ISSUED,
                total,
                lines
        ));
    }


    private List<Sale> payableSalesOfSession(CafeSession session, Customer customer) {
        var sales = salesOfSession(session, customer);
        var openDebts = debtRepository.findByCustomerId(customer.id()).stream()
                .filter(debt -> debt.status() == DebtStatus.OPEN)
                .toList();
        return sales.stream()
                .filter(sale -> openDebts.stream().noneMatch(debt ->
                        debt.isForSale(sale.id())))
                .toList();
    }


    private List<Sale> salesOfSession(CafeSession session, Customer customer) {
        LocalDateTime referenceTime = session.endedAt() == null ? LocalDateTime.now() : session.endedAt();
        return saleRepository.findByCustomerId(customer.id()).stream()
                .filter(sale -> session.id().value().equals(sale.sessionId())
                        || (sale.sessionId() == null && !sale.soldAt().isBefore(session.startedAt()) && !sale.soldAt().isAfter(referenceTime)))
                .toList();
    }

}

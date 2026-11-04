package com.cybermanager.application.usecases.session;

import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.commands.session.DeleteSessionEntryCommand;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.ports.customer.DebtRepository;
import com.cybermanager.domain.ports.sales.SaleRepository;
import com.cybermanager.domain.ports.session.CafeSessionRepository;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.customer.DebtId;
import com.cybermanager.domain.valueobjects.customer.DebtStatus;
import com.cybermanager.domain.valueobjects.sales.SaleId;
import com.cybermanager.domain.valueobjects.sales.SaleType;
import com.cybermanager.domain.valueobjects.session.SessionId;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteSessionEntryUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteSessionEntryUseCase.class);

    private final CafeSessionRepository sessionRepository;
    private final SaleRepository saleRepository;
    private final DebtRepository debtRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionOfferRepository offerRepository;

    public DeleteSessionEntryUseCase(CafeSessionRepository sessionRepository, SaleRepository saleRepository,
                                     DebtRepository debtRepository, CustomerRepository customerRepository,
                                     SubscriptionOfferRepository offerRepository) {
        this.sessionRepository = sessionRepository;
        this.saleRepository = saleRepository;
        this.debtRepository = debtRepository;
        this.customerRepository = customerRepository;
        this.offerRepository = offerRepository;
    }

    public void execute(DeleteSessionEntryCommand command) {
        var session = sessionRepository.findById(new SessionId(command.sessionId()))
                .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.SESSION_NOT_FOUND, "Session not found"));
        Sale sale = null;
        if (command.debtId() != null) {
            var debt = debtRepository.findById(new DebtId(command.debtId()))
                    .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.DEBT_NOT_FOUND, "Debt not found"));
            validateCustomer(session.customerId(), debt.customerId());
            if (debt.status() != DebtStatus.OPEN) {
                throw new BusinessException(BusinessErrorType.CONFLICT, BusinessErrorCode.DEBT_ALREADY_SETTLED, "Debt is already settled");
            }
            if (debt.saleId() != null) {
                sale = saleRepository.findById(new SaleId(debt.saleId())).orElse(null);
            }
            if (sale == null) {
                debtRepository.deleteById(debt.id());
            }
        } else {
            sale = saleRepository.findById(new SaleId(command.saleId()))
                    .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.SALE_NOT_FOUND, "Sale not found"));
            if (sale.sessionId() != null && !sale.sessionId().equals(command.sessionId())) {
                throw new BusinessException(BusinessErrorType.CONFLICT, BusinessErrorCode.SESSION_CUSTOMER_MISMATCH, "Sale does not belong to this session");
            }
        }
        if (sale != null) {
            validateCustomer(session.customerId(), sale.customerId());
            removeSale(sale);
        }
        LOGGER.info("Session entry deleted sessionId={} saleId={} debtId={}", command.sessionId(), command.saleId(), command.debtId());
    }

    private void validateCustomer(CustomerId expected, CustomerId actual) {
        if (!expected.equals(actual)) {
            throw new BusinessException(BusinessErrorType.CONFLICT, BusinessErrorCode.SESSION_CUSTOMER_MISMATCH, "Entry does not belong to the session customer");
        }
    }

    private void removeSale(Sale sale) {
        if (sale.type() == SaleType.SUBSCRIPTION) {
            var customer = customerRepository.findById(sale.customerId())
                    .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));
            int minutes = sale.lines().stream().mapToInt(line -> includedMinutes(line.label()) * line.quantity()).sum();
            customerRepository.save(customer.removeSubscriptionMinutes(minutes));
        }
        debtRepository.findByCustomerId(sale.customerId()).stream()
                .filter(debt -> debt.status() == DebtStatus.OPEN && debt.isForSale(sale.id()))
                .forEach(debt -> debtRepository.deleteById(debt.id()));
        saleRepository.deleteById(sale.id());
    }

    private int includedMinutes(String label) {
        return offerRepository.search(label, SubscriptionOfferStatus.ACTIVE).stream()
                .filter(offer -> offer.name().equalsIgnoreCase(label)).findFirst()
                .or(() -> offerRepository.search(label, SubscriptionOfferStatus.INACTIVE).stream()
                        .filter(offer -> offer.name().equalsIgnoreCase(label)).findFirst())
                .map(offer -> offer.includedMinutes())
                .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND,
                        BusinessErrorCode.SUBSCRIPTION_OFFER_NOT_FOUND, "Subscription offer not found"));
    }

}

package com.cybermanager.application.usecases.sales;

import com.cybermanager.application.services.sales.InvoiceNumberGenerator;
import com.cybermanager.application.services.sales.InvoicePdfRenderer;
import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.application.views.sales.InvoiceDetailView;
import com.cybermanager.application.views.sales.InvoiceLineView;
import com.cybermanager.application.views.sales.InvoicePdfView;
import com.cybermanager.application.views.sales.InvoiceSummaryView;
import com.cybermanager.domain.businessobjects.sales.Invoice;
import com.cybermanager.domain.commands.sales.CreateManualInvoiceCommand;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.ports.sales.InvoiceRepository;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.sales.InvoiceLine;
import com.cybermanager.domain.valueobjects.sales.InvoiceStatus;
import com.cybermanager.domain.valueobjects.shared.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CreateManualInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final InvoicePdfRenderer invoicePdfRenderer;
    private final InvoiceNumberGenerator invoiceNumberGenerator;

    public CreateManualInvoiceUseCase(
            InvoiceRepository invoiceRepository,
            CustomerRepository customerRepository,
            InvoicePdfRenderer invoicePdfRenderer,
            InvoiceNumberGenerator invoiceNumberGenerator
    ) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.invoicePdfRenderer = invoicePdfRenderer;
        this.invoiceNumberGenerator = invoiceNumberGenerator;
    }


    public InvoicePdfView execute(CreateManualInvoiceCommand command) {
        String customerName = normalizeRequired(command.customerName(), BusinessErrorCode.INVOICE_CUSTOMER_REQUIRED, "Le client de la facture est requis.");
        if (command.customerId() != null && customerRepository.findById(new CustomerId(command.customerId())).isEmpty()) {
            throw new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.CUSTOMER_NOT_FOUND, "Customer not found");
        }
        if (command.lines() == null || command.lines().isEmpty()) {
            throw new BusinessException(BusinessErrorType.VALIDATION, BusinessErrorCode.INVOICE_LINES_REQUIRED, "Au moins une ligne de facture est requise.");
        }

        List<InvoiceLine> lines = new ArrayList<>();
        for (var line : command.lines()) {
            String label = normalizeRequired(line.label(), BusinessErrorCode.INVOICE_LINE_LABEL_REQUIRED, "Le libelle de ligne est requis.");
            if (line.quantity() <= 0) {
                throw new BusinessException(BusinessErrorType.VALIDATION, BusinessErrorCode.INVOICE_LINE_QUANTITY_INVALID, "La quantite de ligne doit etre positive.");
            }
            if (line.unitPrice() == null || line.unitPrice().signum() < 0) {
                throw new BusinessException(BusinessErrorType.VALIDATION, BusinessErrorCode.INVOICE_LINE_PRICE_INVALID, "Le prix unitaire doit etre valide.");
            }
            Money unitPrice = new Money(line.unitPrice());
            lines.add(new InvoiceLine(label, line.quantity(), unitPrice, new Money(line.unitPrice().multiply(java.math.BigDecimal.valueOf(line.quantity())))));
        }

        Money total = lines.stream()
                .map(InvoiceLine::totalPrice)
                .reduce(Money.of("0"), Money::add);

        Invoice invoice = invoiceRepository.save(new Invoice(
                java.util.UUID.randomUUID(),
                null,
                invoiceNumberGenerator.next(),
                command.customerId() == null ? null : new CustomerId(command.customerId()),
                customerName,
                LocalDateTime.now(),
                InvoiceStatus.ISSUED,
                total,
                lines
        ));
        return toPdfView(invoice);
    }


    private InvoiceSummaryView toSummaryView(Invoice invoice) {
        return new InvoiceSummaryView(
                invoice.id(),
                invoice.saleId(),
                invoice.invoiceNumber(),
                invoice.customerName(),
                invoice.issuedAt(),
                invoice.status().name(),
                invoice.totalAmount().amount()
        );
    }


    private InvoiceDetailView toDetailView(Invoice invoice) {
        return new InvoiceDetailView(
                invoice.id(),
                invoice.saleId(),
                invoice.invoiceNumber(),
                invoice.customerId() == null ? null : invoice.customerId().value(),
                invoice.customerName(),
                invoice.issuedAt(),
                invoice.status().name(),
                invoice.totalAmount().amount(),
                invoice.lines().stream()
                        .map(line -> new InvoiceLineView(line.label(), line.quantity(), line.unitPrice().amount(), line.totalPrice().amount()))
                        .toList()
        );
    }


    private InvoicePdfView toPdfView(Invoice invoice) {
        return new InvoicePdfView(
                invoice.id(),
                invoice.invoiceNumber(),
                invoice.invoiceNumber() + ".pdf",
                "application/pdf",
                invoicePdfRenderer.render(invoice)
        );
    }


    private String normalizeRequired(String value, BusinessErrorCode code, String message) {
        if (value == null || value.trim().isBlank()) {
            throw new BusinessException(BusinessErrorType.VALIDATION, code, message);
        }
        return value.trim();
    }

}

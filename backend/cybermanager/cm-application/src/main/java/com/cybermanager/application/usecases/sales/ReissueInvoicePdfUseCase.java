package com.cybermanager.application.usecases.sales;

import com.cybermanager.application.services.sales.InvoiceNumberGenerator;
import com.cybermanager.application.services.sales.InvoicePdfRenderer;
import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.application.views.sales.InvoiceDetailView;
import com.cybermanager.application.views.sales.InvoiceLineView;
import com.cybermanager.application.views.sales.InvoicePdfView;
import com.cybermanager.application.views.sales.InvoiceSummaryView;
import com.cybermanager.domain.businessobjects.sales.Invoice;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.ports.sales.InvoiceRepository;
import com.cybermanager.domain.queries.sales.ReissueInvoicePdfQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReissueInvoicePdfUseCase {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final InvoicePdfRenderer invoicePdfRenderer;
    private final InvoiceNumberGenerator invoiceNumberGenerator;

    public ReissueInvoicePdfUseCase(
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


    @Transactional(readOnly = true)
    public InvoicePdfView execute(ReissueInvoicePdfQuery query) {
        Invoice invoice = invoiceRepository.findById(query.invoiceId())
                .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.INVOICE_NOT_FOUND, "Invoice not found"));
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

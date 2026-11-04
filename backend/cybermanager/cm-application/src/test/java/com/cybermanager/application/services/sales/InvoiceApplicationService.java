package com.cybermanager.application.services.sales;

import com.cybermanager.application.usecases.sales.GetInvoiceDetailUseCase;
import com.cybermanager.application.usecases.sales.SearchInvoicesUseCase;
import com.cybermanager.application.views.sales.InvoiceDetailView;
import com.cybermanager.application.views.sales.InvoiceSummaryView;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.ports.sales.InvoiceRepository;
import com.cybermanager.domain.queries.sales.GetInvoiceDetailQuery;
import com.cybermanager.domain.queries.sales.SearchInvoicesQuery;

import java.util.List;

class InvoiceApplicationService {

    private final SearchInvoicesUseCase searchInvoicesUseCase;
    private final GetInvoiceDetailUseCase getInvoiceDetailUseCase;

    InvoiceApplicationService(
            InvoiceRepository invoiceRepository,
            CustomerRepository customerRepository,
            InvoicePdfRenderer invoicePdfRenderer,
            InvoiceNumberGenerator invoiceNumberGenerator
    ) {
        this.searchInvoicesUseCase = new SearchInvoicesUseCase(invoiceRepository, customerRepository, invoicePdfRenderer, invoiceNumberGenerator);
        this.getInvoiceDetailUseCase = new GetInvoiceDetailUseCase(invoiceRepository, customerRepository, invoicePdfRenderer, invoiceNumberGenerator);
    }

    List<InvoiceSummaryView> execute(SearchInvoicesQuery query) {
        return searchInvoicesUseCase.execute(query);
    }

    InvoiceDetailView execute(GetInvoiceDetailQuery query) {
        return getInvoiceDetailUseCase.execute(query);
    }

}

package com.cybermanager.application.services.sales;

import com.cybermanager.domain.businessobjects.sales.Invoice;
import com.cybermanager.domain.ports.company.CompanyProfileRepository;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.sales.InvoiceLine;
import com.cybermanager.domain.valueobjects.sales.InvoiceStatus;
import com.cybermanager.domain.valueobjects.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InvoicePdfRendererTest {

    @Test
    void shouldRenderInvoiceAsPdfBytesWithoutCompanyProfile() {
        CompanyProfileRepository companyProfileRepository = mock(CompanyProfileRepository.class);
        InvoicePdfRenderer renderer = new InvoicePdfRenderer(companyProfileRepository);

        when(companyProfileRepository.findCurrent()).thenReturn(Optional.empty());

        byte[] result = renderer.render(invoice());

        assertTrue(result.length > 0);
        assertTrue(result[0] == '%' && result[1] == 'P' && result[2] == 'D' && result[3] == 'F');
    }

    private Invoice invoice() {
        return new Invoice(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "FAC-20260817-184500-ABCDEF12",
                new CustomerId(UUID.randomUUID()),
                "Alice Martin",
                LocalDateTime.of(2026, 8, 17, 18, 45),
                InvoiceStatus.ISSUED,
                new Money(BigDecimal.TEN),
                List.of(new InvoiceLine("Connexion", 1, new Money(BigDecimal.TEN), new Money(BigDecimal.TEN)))
        );
    }

}

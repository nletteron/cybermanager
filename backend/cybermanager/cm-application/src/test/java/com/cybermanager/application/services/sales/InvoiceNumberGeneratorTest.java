package com.cybermanager.application.services.sales;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceNumberGeneratorTest {

    @Test
    void shouldGenerateReadableInvoiceNumber() {
        String result = new InvoiceNumberGenerator().next();

        assertTrue(result.matches("FAC-\\d{8}-\\d{6}-[A-F0-9]{8}"));
    }

}

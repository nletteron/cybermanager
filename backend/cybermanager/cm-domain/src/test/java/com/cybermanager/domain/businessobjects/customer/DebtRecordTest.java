package com.cybermanager.domain.businessobjects.customer;

import com.cybermanager.domain.ContractTestSupport;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.sales.SaleId;
import com.cybermanager.domain.valueobjects.shared.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DebtRecordTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(DebtRecord.class);
    }

    @Test
    void shouldRetainSaleIdentityWhenSettlingWithoutMatchingOtherIdenticalSales() {
        SaleId saleId = SaleId.newId();
        var debt = DebtRecord.createForSale(CustomerId.newId(), "Produit 1", Money.of("2"), LocalDateTime.now(), saleId.value());

        assertTrue(debt.isForSale(saleId));
        assertFalse(debt.isForSale(SaleId.newId()));
        assertEquals(saleId.value(), debt.settle(LocalDateTime.now(), "Reglee").saleId());
        assertThrows(NullPointerException.class, () -> DebtRecord.createForSale(debt.customerId(), "Produit", Money.of("2"), LocalDateTime.now(), null));
    }

}

package com.cybermanager.domain.businessobjects.catalog;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    void shouldCreateCategoryWithGeneratedIdAndVat() {
        Category category = Category.create("Boissons", "Boissons vendues au comptoir", new BigDecimal("20.00"));
        assertNotNull(category.id().value());
        assertEquals("Boissons", category.name());
        assertEquals(0, category.vatRate().compareTo(new BigDecimal("20")));
    }

    @Test
    void shouldRejectVatOutsideLegalPercentageRange() {
        assertThrows(IllegalArgumentException.class, () -> Category.create("Test", "Description", new BigDecimal("100.01")));
    }

}

package com.cybermanager.domain.businessobjects.catalog;

import com.cybermanager.domain.valueobjects.catalog.CategoryId;

import java.math.BigDecimal;
import java.util.Objects;

public record Category(CategoryId id, String name, String description, BigDecimal vatRate) {

    public Category {
        Objects.requireNonNull(id, "category id is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name is required");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Category description is required");
        Objects.requireNonNull(vatRate, "VAT rate is required");
        if (vatRate.signum() < 0 || vatRate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("VAT rate must be between 0 and 100");
        }
        name = name.trim();
        description = description.trim();
        vatRate = vatRate.stripTrailingZeros();
    }

    public static Category create(String name, String description, BigDecimal vatRate) {
        return new Category(CategoryId.newId(), name, description, vatRate);
    }

}

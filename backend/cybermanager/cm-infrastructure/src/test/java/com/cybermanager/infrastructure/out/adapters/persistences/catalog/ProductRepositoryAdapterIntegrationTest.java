package com.cybermanager.infrastructure.out.adapters.persistences.catalog;

import com.cybermanager.domain.businessobjects.catalog.Category;
import com.cybermanager.domain.businessobjects.catalog.Product;
import com.cybermanager.domain.valueobjects.catalog.ProductStatus;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductRepositoryAdapterIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private CategoryRepositoryAdapter categories;
    @Autowired
    private ProductRepositoryAdapter adapter;

    @Test
    void shouldSaveFindAndSearchProductWithCategoryReference() {
        Category category = categories.save(Category.create("Snacks", "Petite restauration", new BigDecimal("10.00")));
        Product saved = adapter.save(Product.create("Chips", Money.of("1.20"), category.id(), category.name(), "Sachet"));

        var found = adapter.findById(saved.id());
        var active = adapter.search("chi", ProductStatus.ACTIVE, "SNACKS");

        assertTrue(found.isPresent());
        assertEquals(category.id(), found.get().categoryId());
        assertEquals(1, active.size());
        assertEquals("Chips", active.get(0).name());
    }

    @Test
    void shouldReturnEmptyWhenProductDoesNotExistOrSearchDoesNotMatch() {
        assertTrue(adapter.findById(com.cybermanager.domain.valueobjects.catalog.ProductId.newId()).isEmpty());
        assertTrue(adapter.search("unknown", null, null).isEmpty());
    }

    @Test
    void shouldRejectProductWithMissingCategoryReference() {
        Product product = Product.create("Cafe", Money.of("1.20"), com.cybermanager.domain.valueobjects.catalog.CategoryId.newId(), "Boissons", "Expresso");

        assertThrows(JpaObjectRetrievalFailureException.class, () -> adapter.save(product));
    }

}

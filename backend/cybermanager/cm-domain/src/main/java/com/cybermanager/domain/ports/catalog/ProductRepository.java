package com.cybermanager.domain.ports.catalog;

import com.cybermanager.domain.businessobjects.catalog.Product;
import com.cybermanager.domain.valueobjects.catalog.ProductId;
import com.cybermanager.domain.valueobjects.catalog.ProductStatus;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(ProductId productId);

    void deleteById(ProductId productId);

    List<Product> search(String term, ProductStatus status, String category);

}


package com.cybermanager.application.views.catalog;
import java.math.BigDecimal;
import java.util.UUID;
public record CategoryView(UUID categoryId, String name, String description, BigDecimal vatRate) {

}

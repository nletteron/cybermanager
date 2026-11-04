package com.cybermanager.api.dtos.catalog;

import java.math.BigDecimal;

public record CategoryRequest(String name, String description, BigDecimal vatRate) {

}

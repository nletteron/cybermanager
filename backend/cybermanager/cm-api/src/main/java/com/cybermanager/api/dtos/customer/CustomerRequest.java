package com.cybermanager.api.dtos.customer;

import java.util.UUID;

public record CustomerRequest(String name, String type, UUID subscriptionOfferId) {

}

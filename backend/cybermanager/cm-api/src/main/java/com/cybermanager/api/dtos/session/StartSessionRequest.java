package com.cybermanager.api.dtos.session;

import java.util.UUID;

public record StartSessionRequest(UUID customerId, String customerName) {

}

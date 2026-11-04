package com.cybermanager.api.dtos.customer;

import java.util.UUID;

public record ConvertToSubscriberRequest(UUID subscriptionOfferId, boolean deductCurrentSession, UUID sessionId) {

}

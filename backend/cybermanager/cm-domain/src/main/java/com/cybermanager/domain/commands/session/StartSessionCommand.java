package com.cybermanager.domain.commands.session;

import java.util.UUID;

public record StartSessionCommand(UUID customerId, String customerName) {

}

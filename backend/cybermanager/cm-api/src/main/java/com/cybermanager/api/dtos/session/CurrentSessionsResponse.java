package com.cybermanager.api.dtos.session;

import java.util.List;

public record CurrentSessionsResponse(List<SessionResponse> sessions) {

}

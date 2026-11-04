package com.cybermanager.application.views.monitoring;

import java.time.LocalDateTime;

public record SessionActivityView(String sessionLabel, LocalDateTime startedAt, LocalDateTime endedAt) {

}

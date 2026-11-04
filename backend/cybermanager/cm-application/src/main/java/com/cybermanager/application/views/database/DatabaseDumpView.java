package com.cybermanager.application.views.database;

import java.time.Instant;

public record DatabaseDumpView(String fileName, long size, Instant modifiedAt) {

}

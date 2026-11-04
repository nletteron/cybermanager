package com.cybermanager.api.dtos.database;

import java.time.Instant;

public record DatabaseDumpResponse(String fileName, long size, Instant modifiedAt) {

}

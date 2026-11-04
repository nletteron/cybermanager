package com.cybermanager.application.ports.database;

import java.time.Instant;

public record DumpResource(String fileName, long size, Instant modifiedAt) {

}

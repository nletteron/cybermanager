package com.cybermanager.application.ports.database;

import java.io.InputStream;

public record DumpContent(String fileName, long size, InputStream content) {

}

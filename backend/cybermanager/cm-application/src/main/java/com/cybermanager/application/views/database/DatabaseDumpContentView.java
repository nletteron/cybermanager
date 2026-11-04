package com.cybermanager.application.views.database;

import java.io.InputStream;

public record DatabaseDumpContentView(String fileName, long size, InputStream content) {

}

package com.cybermanager.application.ports.database;

import java.io.InputStream;
import java.util.List;

public interface DatabaseBackupGateway {

    List<DumpResource> list();

    DumpResource exportDatabase();

    DumpResource store(String fileName, InputStream content);

    DumpContent read(String fileName);

    void restore(String fileName);

}

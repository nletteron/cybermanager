package com.cybermanager.domain.ports.customer;

import com.cybermanager.domain.businessobjects.customer.GeneratedArchiveFile;

import java.util.List;

public interface GeneratedArchiveFileRepository {

    GeneratedArchiveFile save(GeneratedArchiveFile file);

    List<GeneratedArchiveFile> findRecent();

}

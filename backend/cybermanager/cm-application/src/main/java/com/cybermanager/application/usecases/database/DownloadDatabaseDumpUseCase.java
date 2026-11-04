package com.cybermanager.application.usecases.database;

import com.cybermanager.application.ports.database.DatabaseBackupGateway;
import com.cybermanager.application.ports.database.DumpResource;
import com.cybermanager.application.services.shared.ActorSupport;
import com.cybermanager.application.views.database.DatabaseDumpContentView;
import com.cybermanager.application.views.database.DatabaseDumpView;
import com.cybermanager.domain.queries.database.DownloadDatabaseDumpQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DownloadDatabaseDumpUseCase {

    private final DatabaseBackupGateway gateway;

    public DownloadDatabaseDumpUseCase(DatabaseBackupGateway gateway) {
        this.gateway = gateway;
    }


    public DatabaseDumpContentView execute(DownloadDatabaseDumpQuery query) {
        ActorSupport.requireAdmin(query.actorRoles());
        var content = gateway.read(query.fileName());
        return new DatabaseDumpContentView(content.fileName(), content.size(), content.content());
    }


    private DatabaseDumpView toView(DumpResource resource) {
        return new DatabaseDumpView(resource.fileName(), resource.size(), resource.modifiedAt());
    }

}

package com.cybermanager.application.usecases.database;

import com.cybermanager.application.ports.database.DatabaseBackupGateway;
import com.cybermanager.application.ports.database.DumpResource;
import com.cybermanager.application.services.shared.ActorSupport;
import com.cybermanager.application.views.database.DatabaseDumpView;
import com.cybermanager.domain.commands.database.ExportDatabaseCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExportDatabaseUseCase {

    private final DatabaseBackupGateway gateway;

    public ExportDatabaseUseCase(DatabaseBackupGateway gateway) {
        this.gateway = gateway;
    }


    public DatabaseDumpView execute(ExportDatabaseCommand command) {
        ActorSupport.requireAdmin(command.actorRoles());
        return toView(gateway.exportDatabase());
    }


    private DatabaseDumpView toView(DumpResource resource) {
        return new DatabaseDumpView(resource.fileName(), resource.size(), resource.modifiedAt());
    }

}

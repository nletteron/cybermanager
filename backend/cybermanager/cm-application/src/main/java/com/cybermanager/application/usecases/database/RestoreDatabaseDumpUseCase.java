package com.cybermanager.application.usecases.database;

import com.cybermanager.application.ports.database.DatabaseBackupGateway;
import com.cybermanager.application.ports.database.DumpResource;
import com.cybermanager.application.services.shared.ActorSupport;
import com.cybermanager.application.views.database.DatabaseDumpView;
import com.cybermanager.domain.commands.database.RestoreDatabaseDumpCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RestoreDatabaseDumpUseCase {

    private final DatabaseBackupGateway gateway;

    public RestoreDatabaseDumpUseCase(DatabaseBackupGateway gateway) {
        this.gateway = gateway;
    }


    public void execute(RestoreDatabaseDumpCommand command) {
        ActorSupport.requireAdmin(command.actorRoles());
        gateway.restore(command.fileName());
    }


    private DatabaseDumpView toView(DumpResource resource) {
        return new DatabaseDumpView(resource.fileName(), resource.size(), resource.modifiedAt());
    }

}

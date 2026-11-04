package com.cybermanager.application.usecases.database;

import com.cybermanager.application.ports.database.DatabaseBackupGateway;
import com.cybermanager.application.ports.database.DumpResource;
import com.cybermanager.application.services.shared.ActorSupport;
import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.application.views.database.DatabaseDumpView;
import com.cybermanager.domain.commands.database.UploadDatabaseDumpCommand;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UploadDatabaseDumpUseCase {

    private final DatabaseBackupGateway gateway;

    public UploadDatabaseDumpUseCase(DatabaseBackupGateway gateway) {
        this.gateway = gateway;
    }


    public DatabaseDumpView execute(UploadDatabaseDumpCommand command) {
        ActorSupport.requireAdmin(command.actorRoles());
        if (command.content() == null || command.fileName() == null || command.fileName().isBlank()) {
            throw new BusinessException(BusinessErrorType.VALIDATION, BusinessErrorCode.DATABASE_DUMP_REQUIRED, "Un fichier dump est requis.");
        }
        return toView(gateway.store(command.fileName(), command.content()));
    }


    private DatabaseDumpView toView(DumpResource resource) {
        return new DatabaseDumpView(resource.fileName(), resource.size(), resource.modifiedAt());
    }

}

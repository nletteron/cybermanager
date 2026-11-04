package com.cybermanager.application.usecases.database;

import com.cybermanager.application.ports.database.DatabaseBackupGateway;
import com.cybermanager.application.ports.database.DumpResource;
import com.cybermanager.application.services.shared.ActorSupport;
import com.cybermanager.application.views.database.DatabaseDumpView;
import com.cybermanager.domain.queries.database.ListDatabaseDumpsQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ListDatabaseDumpsUseCase {

    private final DatabaseBackupGateway gateway;

    public ListDatabaseDumpsUseCase(DatabaseBackupGateway gateway) {
        this.gateway = gateway;
    }


    public List<DatabaseDumpView> execute(ListDatabaseDumpsQuery query) {
        ActorSupport.requireAdmin(query.actorRoles());
        return gateway.list().stream().map(this::toView).toList();
    }


    private DatabaseDumpView toView(DumpResource resource) {
        return new DatabaseDumpView(resource.fileName(), resource.size(), resource.modifiedAt());
    }

}

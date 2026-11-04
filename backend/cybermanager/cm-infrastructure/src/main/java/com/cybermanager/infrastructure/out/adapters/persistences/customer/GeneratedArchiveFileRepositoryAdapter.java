package com.cybermanager.infrastructure.out.adapters.persistences.customer;

import com.cybermanager.domain.businessobjects.customer.GeneratedArchiveFile;
import com.cybermanager.domain.ports.customer.GeneratedArchiveFileRepository;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.customer.GeneratedArchiveFileJpaRepository;
import com.cybermanager.infrastructure.out.entities.customer.GeneratedArchiveFileJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeneratedArchiveFileRepositoryAdapter implements GeneratedArchiveFileRepository {

    private final GeneratedArchiveFileJpaRepository repository;

    public GeneratedArchiveFileRepositoryAdapter(GeneratedArchiveFileJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public GeneratedArchiveFile save(GeneratedArchiveFile file) {
        GeneratedArchiveFileJpaEntity entity = new GeneratedArchiveFileJpaEntity();
        entity.id = file.id();
        entity.fileName = file.fileName();
        entity.fileType = file.fileType();
        entity.generatedBy = file.generatedBy();
        entity.generatedAt = file.generatedAt();
        return toDomain(repository.save(entity));
    }

    @Override
    public List<GeneratedArchiveFile> findRecent() {
        return repository.findAllByOrderByGeneratedAtDesc().stream().map(this::toDomain).toList();
    }

    private GeneratedArchiveFile toDomain(GeneratedArchiveFileJpaEntity entity) {
        return new GeneratedArchiveFile(entity.id, entity.fileName, entity.fileType, entity.generatedBy, entity.generatedAt);
    }

}



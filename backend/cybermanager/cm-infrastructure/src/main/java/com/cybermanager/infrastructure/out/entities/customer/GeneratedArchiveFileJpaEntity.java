package com.cybermanager.infrastructure.out.entities.customer;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "cm_generated_archive_files",
        indexes = {
                @Index(name = "idx_cm_generated_archive_files_generated_at", columnList = "generatedAt")
        }
)
public class GeneratedArchiveFileJpaEntity {

    @Id
    public UUID id;
    public String fileName;
    public String fileType;
    public String generatedBy;
    public LocalDateTime generatedAt;

}

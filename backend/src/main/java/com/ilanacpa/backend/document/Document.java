package com.ilanacpa.backend.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
public class Document implements Persistable<UUID> {

    // Assigned identifier (see DocumentService.upload), not @UuidGenerator: Hibernate
    // treats a generator-annotated field with a pre-set id as a detached entity, not
    // a new one, and rejects persist() with "Detached entity passed to persist".
    @Id
    private UUID id;

    // Since the id is always non-null by the time save() is called, Spring Data's
    // default "id == null means new" check would otherwise treat this as an existing
    // row and issue an UPDATE instead of an INSERT.
    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        isNew = false;
    }

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status = DocumentStatus.pending;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @jakarta.persistence.PrePersist
    void prePersist() {
        Instant now = Instant.now();
        uploadedAt = now;
        updatedAt = now;
    }

    @jakarta.persistence.PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}

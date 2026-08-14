package com.ilanacpa.backend.audit;

import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void log(AuditAction action, UUID actorId, String entityType, String entityId,
                     Map<String, Object> metadata, String ipAddress) {
        AuditLog entry = new AuditLog();
        entry.setAction(action);
        entry.setActorId(actorId);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setMetadata(metadata);
        entry.setIpAddress(ipAddress);
        repository.save(entry);
    }

    public void log(AuditAction action, UUID actorId, String ipAddress) {
        log(action, actorId, null, null, null, ipAddress);
    }
}

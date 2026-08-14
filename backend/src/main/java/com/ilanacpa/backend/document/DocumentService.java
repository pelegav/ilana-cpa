package com.ilanacpa.backend.document;

import com.ilanacpa.backend.audit.AuditAction;
import com.ilanacpa.backend.audit.AuditService;
import com.ilanacpa.backend.common.ForbiddenException;
import com.ilanacpa.backend.common.NotFoundException;
import com.ilanacpa.backend.security.UserPrincipal;
import com.ilanacpa.backend.user.Role;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

    private static final int SIGNED_URL_TTL_SECONDS = 300;

    private final DocumentRepository documentRepository;
    private final SupabaseStorageService storageService;
    private final AuditService auditService;

    public DocumentService(DocumentRepository documentRepository, SupabaseStorageService storageService,
                            AuditService auditService) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.auditService = auditService;
    }

    public List<Document> listFor(UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN || principal.getRole() == Role.ACCOUNTANT) {
            return documentRepository.findAll();
        }
        return documentRepository.findByOwnerId(principal.getId());
    }

    /**
     * Ownership-checked single-document fetch — the RLS replacement for row-level access.
     * Deliberately an imperative check rather than {@code @PostAuthorize}: this is called
     * from within this same class (see {@link #getDownloadUrl}), and Spring's AOP-based
     * method security proxy does not intercept self-invocation — an annotation here would
     * silently never run, which is exactly how a cross-tenant download bug slipped through
     * until the smoke test caught it.
     */
    public Document getChecked(UserPrincipal principal, UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        boolean isStaff = principal.getRole() == Role.ADMIN || principal.getRole() == Role.ACCOUNTANT;
        if (!isStaff && !document.getOwnerId().equals(principal.getId())) {
            throw new ForbiddenException("You do not have access to this document");
        }
        return document;
    }

    public Document upload(UserPrincipal principal, byte[] content, String fileName, String contentType,
                            String category, String ipAddress) {
        // Id generated up front so the not-null storage_path (which embeds it) can be
        // set before the first insert, instead of needing an insert-then-update.
        UUID documentId = UUID.randomUUID();
        String storagePath = principal.getId() + "/" + documentId + "/" + fileName;

        Document document = new Document();
        document.setId(documentId);
        document.setOwnerId(principal.getId());
        document.setFileName(fileName);
        document.setCategory(category);
        document.setStoragePath(storagePath);

        storageService.upload(storagePath, content, contentType);
        Document saved = documentRepository.save(document);

        auditService.log(AuditAction.DOCUMENT_UPLOADED, principal.getId(), "Document", saved.getId().toString(),
                Map.of("fileName", fileName), ipAddress);
        return saved;
    }

    public String getDownloadUrl(UserPrincipal principal, UUID documentId, String ipAddress) {
        Document document = getChecked(principal, documentId);
        String url = storageService.createSignedDownloadUrl(document.getStoragePath(), SIGNED_URL_TTL_SECONDS);
        auditService.log(AuditAction.DOCUMENT_DOWNLOADED, principal.getId(), "Document", documentId.toString(),
                null, ipAddress);
        return url;
    }

    public Document updateStatus(UserPrincipal principal, UUID documentId, DocumentStatus status, String ipAddress) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        document.setStatus(status);
        Document saved = documentRepository.save(document);
        auditService.log(AuditAction.DOCUMENT_STATUS_CHANGED, principal.getId(), "Document", documentId.toString(),
                Map.of("status", status.name()), ipAddress);
        return saved;
    }
}

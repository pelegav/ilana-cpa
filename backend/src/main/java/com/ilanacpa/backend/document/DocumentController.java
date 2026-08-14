package com.ilanacpa.backend.document;

import com.ilanacpa.backend.document.dto.SignedUrlResponse;
import com.ilanacpa.backend.document.dto.UpdateDocumentStatusRequest;
import com.ilanacpa.backend.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<Document> list(@AuthenticationPrincipal UserPrincipal principal) {
        return documentService.listFor(principal);
    }

    @PostMapping
    public Document upload(@AuthenticationPrincipal UserPrincipal principal,
                            @RequestParam("file") MultipartFile file,
                            @RequestParam(value = "category", required = false) String category,
                            HttpServletRequest httpRequest) throws IOException {
        return documentService.upload(principal, file.getBytes(), file.getOriginalFilename(),
                file.getContentType(), category, httpRequest.getRemoteAddr());
    }

    @GetMapping("/{id}/download")
    public SignedUrlResponse download(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id,
                                       HttpServletRequest httpRequest) {
        return new SignedUrlResponse(documentService.getDownloadUrl(principal, id, httpRequest.getRemoteAddr()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public Document updateStatus(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id,
                                  @Valid @RequestBody UpdateDocumentStatusRequest request,
                                  HttpServletRequest httpRequest) {
        return documentService.updateStatus(principal, id, request.status(), httpRequest.getRemoteAddr());
    }
}

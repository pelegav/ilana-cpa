package com.ilanacpa.backend.document.dto;

import com.ilanacpa.backend.document.DocumentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDocumentStatusRequest(@NotNull DocumentStatus status) {
}

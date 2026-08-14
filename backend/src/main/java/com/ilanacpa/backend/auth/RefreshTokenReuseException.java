package com.ilanacpa.backend.auth;

import java.util.UUID;

/** Raised when a revoked (already-rotated) refresh token is presented outside the
 * concurrent-refresh grace window — a signal of possible token theft. The entire
 * token family has been revoked by the time this is thrown. */
public class RefreshTokenReuseException extends RuntimeException {

    private final UUID userId;

    public RefreshTokenReuseException(UUID userId) {
        super("Refresh token reuse detected");
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}

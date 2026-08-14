package com.ilanacpa.backend.auth;

import com.ilanacpa.backend.security.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// RefreshTokenReuseException must NOT roll back: the whole point of rotate()'s theft
// path is to persist the family revocation, then report theft to the caller. Spring's
// default "roll back on any RuntimeException" would otherwise silently undo it.
@Transactional(noRollbackFor = RefreshTokenReuseException.class)
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtProperties properties;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository repository, JwtProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public record RotationResult(String rawToken, UUID userId) {
    }

    /** Issues the first refresh token of a brand-new family (e.g. on login). */
    public String issue(UUID userId) {
        UUID familyId = UUID.randomUUID();
        return createToken(userId, familyId).rawToken();
    }

    /**
     * Validates and rotates a presented refresh token.
     *
     * @throws RefreshTokenReuseException if reuse is detected outside the grace window
     *         (the whole family has already been revoked by the time this is thrown).
     * @throws IllegalArgumentException if the token is unknown or genuinely expired.
     */
    public RotationResult rotate(String rawToken) {
        String hash = hash(rawToken);
        RefreshToken presented = repository.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Unknown refresh token"));

        if (presented.isActive()) {
            return rotateFrom(presented);
        }

        if (presented.getRevokedAt() != null && presented.getReplacedByTokenId() != null) {
            boolean withinGrace = presented.getRevokedAt()
                    .plusSeconds(properties.refreshReuseGraceSeconds())
                    .isAfter(Instant.now());
            if (withinGrace) {
                var currentHead = repository.findByFamilyIdAndRevokedAtIsNull(presented.getFamilyId());
                if (currentHead.isPresent() && currentHead.get().isActive()) {
                    return rotateFrom(currentHead.get());
                }
            }
        }

        // Revoked outside the grace window (or no live head token left): treat as theft.
        repository.revokeFamily(presented.getFamilyId(), Instant.now());
        throw new RefreshTokenReuseException(presented.getUserId());
    }

    public void revokeFamilyOf(String rawToken) {
        repository.findByTokenHash(hash(rawToken))
                .ifPresent(t -> repository.revokeFamily(t.getFamilyId(), Instant.now()));
    }

    public void revokeAllForUser(UUID userId) {
        repository.revokeAllForUser(userId, Instant.now());
    }

    private RotationResult rotateFrom(RefreshToken current) {
        CreatedToken next = createToken(current.getUserId(), current.getFamilyId());
        current.setRevokedAt(Instant.now());
        current.setReplacedByTokenId(next.entity().getId());
        repository.save(current);
        return new RotationResult(next.rawToken(), current.getUserId());
    }

    private record CreatedToken(String rawToken, RefreshToken entity) {
    }

    private CreatedToken createToken(UUID userId, UUID familyId) {
        String rawToken = generateRawToken();
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setFamilyId(familyId);
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(Instant.now().plusSeconds(properties.refreshTokenTtlDays() * 24 * 60 * 60));
        RefreshToken saved = repository.save(token);
        return new CreatedToken(rawToken, saved);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

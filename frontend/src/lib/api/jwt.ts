export type JwtClaims = {
  sub: string;
  email: string;
  role: "ADMIN" | "ACCOUNTANT" | "CLIENT";
  iat: number;
  exp: number;
};

/**
 * Decodes a JWT payload WITHOUT verifying its signature. Only safe for optimistic,
 * non-authoritative checks (e.g. proxy.ts redirect routing) — never trust this for
 * an actual authorization decision. The signature is verified server-side by Spring
 * Boot on every real API call, and layouts re-check via GET /api/auth/me.
 */
export function decodeJwtPayload(token: string): JwtClaims | null {
  try {
    const payload = token.split(".")[1];
    if (!payload) return null;
    const json = Buffer.from(payload, "base64url").toString("utf-8");
    return JSON.parse(json) as JwtClaims;
  } catch {
    return null;
  }
}

export function isExpired(claims: JwtClaims, skewSeconds = 30): boolean {
  return claims.exp * 1000 < Date.now() + skewSeconds * 1000;
}

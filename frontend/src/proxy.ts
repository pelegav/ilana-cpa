import { NextResponse, type NextRequest } from "next/server";
import { decodeJwtPayload, isExpired, type JwtClaims } from "@/lib/api/jwt";

const PORTAL_PREFIX = "/portal";
const ADMIN_PREFIX = "/admin";
const BACKEND_URL = process.env.BACKEND_API_URL ?? "http://localhost:8082";

const cookieOptions = {
  httpOnly: true,
  secure: process.env.NODE_ENV === "production",
  sameSite: "lax" as const,
  path: "/",
};

async function refresh(refreshToken: string): Promise<{ accessToken: string; refreshToken: string } | null> {
  try {
    const res = await fetch(`${BACKEND_URL}/api/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
      cache: "no-store",
    });
    if (!res.ok) return null;
    const data = await res.json();
    return { accessToken: data.accessToken, refreshToken: data.refreshToken };
  } catch {
    return null;
  }
}

export async function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const isPortalRoute = pathname.startsWith(PORTAL_PREFIX);
  const isAdminRoute = pathname.startsWith(ADMIN_PREFIX);

  if (!isPortalRoute && !isAdminRoute) {
    return NextResponse.next();
  }

  let accessToken = request.cookies.get("access_token")?.value;
  const refreshToken = request.cookies.get("refresh_token")?.value;
  let claims: JwtClaims | null = accessToken ? decodeJwtPayload(accessToken) : null;

  let refreshedTokens: { accessToken: string; refreshToken: string } | null = null;
  if ((!claims || isExpired(claims)) && refreshToken) {
    refreshedTokens = await refresh(refreshToken);
    if (refreshedTokens) {
      accessToken = refreshedTokens.accessToken;
      claims = decodeJwtPayload(refreshedTokens.accessToken);
    }
  }

  if (!claims) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("next", pathname);
    const redirectResponse = NextResponse.redirect(loginUrl);
    redirectResponse.cookies.delete("access_token");
    redirectResponse.cookies.delete("refresh_token");
    return redirectResponse;
  }

  if (isAdminRoute && claims.role !== "ADMIN" && claims.role !== "ACCOUNTANT") {
    return NextResponse.redirect(new URL("/portal", request.url));
  }

  // Reflect the (possibly refreshed) access token into the request so downstream
  // Server Components/layouts see it via cookies() in this same request cycle.
  if (refreshedTokens) {
    request.cookies.set("access_token", refreshedTokens.accessToken);
  }
  const response = NextResponse.next({ request });
  if (refreshedTokens) {
    response.cookies.set("access_token", refreshedTokens.accessToken, { ...cookieOptions, maxAge: 15 * 60 });
    response.cookies.set("refresh_token", refreshedTokens.refreshToken, {
      ...cookieOptions,
      maxAge: 30 * 24 * 60 * 60,
    });
  }
  return response;
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};

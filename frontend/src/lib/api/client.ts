import "server-only";
import { cookies } from "next/headers";

export const BACKEND_URL = process.env.BACKEND_API_URL ?? "http://localhost:8082";

export const ACCESS_TOKEN_COOKIE = "access_token";
export const REFRESH_TOKEN_COOKIE = "refresh_token";

const cookieOptions = {
  httpOnly: true,
  secure: process.env.NODE_ENV === "production",
  sameSite: "lax" as const,
  path: "/",
};

export async function setAuthCookies(accessToken: string, refreshToken: string) {
  const store = await cookies();
  store.set(ACCESS_TOKEN_COOKIE, accessToken, { ...cookieOptions, maxAge: 15 * 60 });
  store.set(REFRESH_TOKEN_COOKIE, refreshToken, { ...cookieOptions, maxAge: 30 * 24 * 60 * 60 });
}

export async function clearAuthCookies() {
  const store = await cookies();
  store.delete(ACCESS_TOKEN_COOKIE);
  store.delete(REFRESH_TOKEN_COOKIE);
}

/** Server-to-server call to the Spring Boot API, attaching the caller's access token. */
export async function apiFetch(path: string, init: RequestInit = {}): Promise<Response> {
  const store = await cookies();
  const accessToken = store.get(ACCESS_TOKEN_COOKIE)?.value;

  const headers = new Headers(init.headers);
  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  return fetch(`${BACKEND_URL}${path}`, { ...init, headers, cache: "no-store" });
}

/** Unauthenticated call — for login, which happens before any token exists. */
export async function publicApiFetch(path: string, init: RequestInit = {}): Promise<Response> {
  return fetch(`${BACKEND_URL}${path}`, { ...init, cache: "no-store" });
}

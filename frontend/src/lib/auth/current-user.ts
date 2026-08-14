import "server-only";
import { apiFetch } from "@/lib/api/client";

export type CurrentUser = {
  id: string;
  email: string;
  fullName: string | null;
  role: "ADMIN" | "ACCOUNTANT" | "CLIENT";
};

/** Authoritative session check — verifies the JWT server-side via Spring Boot,
 * unlike proxy.ts's decode-only optimistic check. Returns null if unauthenticated. */
export async function getCurrentUser(): Promise<CurrentUser | null> {
  const res = await apiFetch("/api/auth/me");
  if (!res.ok) return null;
  return res.json();
}

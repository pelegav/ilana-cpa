"use server";

import { redirect } from "next/navigation";
import { cookies } from "next/headers";
import { publicApiFetch, setAuthCookies, clearAuthCookies, REFRESH_TOKEN_COOKIE } from "@/lib/api/client";

export type LoginState = { error?: string } | undefined;

export async function login(_state: LoginState, formData: FormData): Promise<LoginState> {
  const email = String(formData.get("email") ?? "");
  const password = String(formData.get("password") ?? "");
  const next = String(formData.get("next") ?? "");

  const res = await publicApiFetch("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  if (!res.ok) {
    if (res.status === 429) {
      return { error: "Too many attempts. Please try again in a few minutes." };
    }
    return { error: "Invalid email or password." };
  }

  const data = await res.json();
  await setAuthCookies(data.accessToken, data.refreshToken);

  if (next && next.startsWith("/")) {
    redirect(next);
  }

  redirect(data.user.role === "ADMIN" || data.user.role === "ACCOUNTANT" ? "/admin" : "/portal");
}

export async function logout() {
  const store = await cookies();
  const refreshToken = store.get(REFRESH_TOKEN_COOKIE)?.value;

  if (refreshToken) {
    await publicApiFetch("/api/auth/logout", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    }).catch(() => {
      // Best-effort — cookies are cleared regardless below.
    });
  }

  await clearAuthCookies();
  redirect("/login");
}

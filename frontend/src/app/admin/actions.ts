"use server";

import { apiFetch } from "@/lib/api/client";

export type CreateClientState =
  | { error: string }
  | { success: true; email: string; temporaryPassword: string }
  | undefined;

export async function createClientAccount(
  _state: CreateClientState,
  formData: FormData
): Promise<CreateClientState> {
  const email = String(formData.get("email") ?? "").trim();
  const fullName = String(formData.get("full_name") ?? "").trim();

  if (!email) {
    return { error: "Email is required." };
  }

  const res = await apiFetch("/api/admin/users", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, fullName: fullName || null, role: "CLIENT" }),
  });

  if (!res.ok) {
    if (res.status === 403) {
      return { error: "You are not authorized to create accounts." };
    }
    const body = await res.json().catch(() => null);
    return { error: body?.message ?? "Failed to create account." };
  }

  const data = await res.json();
  return { success: true, email: data.user.email, temporaryPassword: data.temporaryPassword };
}

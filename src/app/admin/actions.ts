"use server";

import { randomBytes } from "crypto";
import { createClient, createAdminClient } from "@/lib/supabase/server";

export type CreateClientState =
  | { error: string }
  | { success: true; email: string; temporaryPassword: string }
  | undefined;

async function requireAdmin() {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();
  if (!user) throw new Error("Not authenticated");

  const { data: profile } = await supabase
    .from("profiles")
    .select("role")
    .eq("id", user.id)
    .single();
  if (profile?.role !== "admin") throw new Error("Not authorized");
}

function generateTemporaryPassword() {
  return randomBytes(9).toString("base64url");
}

export async function createClientAccount(
  _state: CreateClientState,
  formData: FormData
): Promise<CreateClientState> {
  await requireAdmin();

  const email = String(formData.get("email") ?? "").trim();
  const fullName = String(formData.get("full_name") ?? "").trim();

  if (!email) {
    return { error: "Email is required." };
  }

  const temporaryPassword = generateTemporaryPassword();
  const admin = createAdminClient();

  const { data, error } = await admin.auth.admin.createUser({
    email,
    password: temporaryPassword,
    email_confirm: true,
  });

  if (error || !data.user) {
    return { error: error?.message ?? "Failed to create account." };
  }

  if (fullName) {
    await admin.from("profiles").update({ full_name: fullName }).eq("id", data.user.id);
  }

  return { success: true, email, temporaryPassword };
}

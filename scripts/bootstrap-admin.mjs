// One-off script to create the first admin account and set its role.
// Usage: node scripts/bootstrap-admin.mjs <email> <password>
import { readFileSync } from "node:fs";
import { createClient } from "@supabase/supabase-js";

function loadEnvLocal() {
  const content = readFileSync(new URL("../.env.local", import.meta.url), "utf-8");
  const env = {};
  for (const line of content.split("\n")) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#")) continue;
    const idx = trimmed.indexOf("=");
    if (idx === -1) continue;
    env[trimmed.slice(0, idx)] = trimmed.slice(idx + 1);
  }
  return env;
}

const env = loadEnvLocal();
const [, , email, password] = process.argv;

if (!email || !password) {
  console.error("Usage: node scripts/bootstrap-admin.mjs <email> <password>");
  process.exit(1);
}

const supabase = createClient(
  env.NEXT_PUBLIC_SUPABASE_URL,
  env.SUPABASE_SERVICE_ROLE_KEY
);

const { data, error } = await supabase.auth.admin.createUser({
  email,
  password,
  email_confirm: true,
});

if (error || !data.user) {
  console.error("Failed to create user:", error?.message);
  process.exit(1);
}

const { error: updateError } = await supabase
  .from("profiles")
  .update({ role: "admin" })
  .eq("id", data.user.id);

if (updateError) {
  console.error("User created but failed to set role=admin:", updateError.message);
  process.exit(1);
}

console.log(`Admin account created: ${email}`);

import { redirect } from "next/navigation";
import { createClient } from "@/lib/supabase/server";
import { PortalHeader } from "@/components/portal/portal-header";

export default async function PortalLayout({ children }: { children: React.ReactNode }) {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();

  if (!user) {
    redirect("/login?next=/portal");
  }

  return (
    <div className="min-h-full bg-slate-50">
      <PortalHeader email={user.email ?? ""} />
      <main className="mx-auto max-w-5xl px-6 py-10">{children}</main>
    </div>
  );
}

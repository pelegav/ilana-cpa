import { redirect } from "next/navigation";
import { createClient } from "@/lib/supabase/server";
import { AdminHeader } from "@/components/admin/admin-header";

export default async function AdminLayout({ children }: { children: React.ReactNode }) {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();

  if (!user) {
    redirect("/login?next=/admin");
  }

  const { data: profile } = await supabase
    .from("profiles")
    .select("role")
    .eq("id", user.id)
    .single();

  if (profile?.role !== "admin") {
    redirect("/portal");
  }

  return (
    <div className="min-h-full bg-slate-50">
      <AdminHeader email={user.email ?? ""} />
      <main className="mx-auto max-w-5xl px-6 py-10">{children}</main>
    </div>
  );
}

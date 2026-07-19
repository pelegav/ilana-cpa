import Link from "next/link";
import { logout } from "@/lib/auth/actions";
import { Button } from "@/components/ui/button";

export function PortalHeader({ email }: { email: string }) {
  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
        <Link href="/portal" className="font-semibold text-slate-900">
          Ilana CPA — Client Area
        </Link>
        <div className="flex items-center gap-4 text-sm text-slate-600">
          <span>{email}</span>
          <form action={logout}>
            <Button variant="secondary" type="submit">
              Sign out
            </Button>
          </form>
        </div>
      </div>
    </header>
  );
}

import Link from "next/link";
import { Button } from "@/components/ui/button";

export default function HomePage() {
  return (
    <div className="mx-auto max-w-5xl px-6 py-24">
      <h1 className="text-4xl font-semibold tracking-tight text-slate-900">
        Ilana CPA
      </h1>
      <p className="mt-4 max-w-2xl text-lg text-slate-600">
        Accounting and tax services. Content on this page is a placeholder —
        final copy, branding, and design are still to come.
      </p>
      <div className="mt-8 flex gap-4">
        <Link href="/contact">
          <Button>Contact us</Button>
        </Link>
        <Link href="/login">
          <Button variant="secondary">Client login</Button>
        </Link>
      </div>
    </div>
  );
}

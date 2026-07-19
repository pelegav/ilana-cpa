import Link from "next/link";

const links = [
  { href: "/", label: "Home" },
  { href: "/services", label: "Services" },
  { href: "/about", label: "About" },
  { href: "/contact", label: "Contact" },
];

export function SiteHeader() {
  return (
    <header className="border-b border-slate-200">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
        <Link href="/" className="font-semibold text-slate-900">
          Ilana CPA
        </Link>
        <nav className="flex items-center gap-6 text-sm text-slate-600">
          {links.map((link) => (
            <Link key={link.href} href={link.href} className="hover:text-slate-900">
              {link.label}
            </Link>
          ))}
          <Link
            href="/login"
            className="rounded-md bg-slate-900 px-3 py-1.5 text-white hover:bg-slate-700"
          >
            Client Login
          </Link>
        </nav>
      </div>
    </header>
  );
}

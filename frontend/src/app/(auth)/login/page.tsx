import { Card } from "@/components/ui/card";
import { LoginForm } from "./login-form";

export default async function LoginPage({
  searchParams,
}: {
  searchParams: Promise<{ next?: string }>;
}) {
  const { next } = await searchParams;

  return (
    <div className="flex min-h-full flex-1 items-center justify-center px-6 py-24">
      <Card className="w-full max-w-sm">
        <h1 className="mb-6 text-xl font-semibold text-slate-900">Client Login</h1>
        <LoginForm next={next} />
      </Card>
    </div>
  );
}

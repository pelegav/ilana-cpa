import { Card } from "@/components/ui/card";
import { CreateClientForm } from "./create-client-form";

export default function AdminDashboardPage() {
  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-900">Admin dashboard</h1>
      <p className="mt-2 text-slate-600">
        Placeholder — client list, document review, and permissions management
        go here once requirements are finalized.
      </p>
      <Card className="mt-6 max-w-md">
        <h2 className="mb-4 text-lg font-medium text-slate-900">Create client account</h2>
        <CreateClientForm />
      </Card>
    </div>
  );
}

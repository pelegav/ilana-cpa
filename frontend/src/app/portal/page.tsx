import { Card } from "@/components/ui/card";

export default function PortalDashboardPage() {
  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-900">Your personal zone</h1>
      <p className="mt-2 text-slate-600">
        Placeholder — document upload and other self-service features go here
        once requirements are finalized.
      </p>
      <Card className="mt-6">
        <p className="text-sm text-slate-500">No documents yet.</p>
      </Card>
    </div>
  );
}

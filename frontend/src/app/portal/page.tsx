import { apiFetch } from "@/lib/api/client";
import type { Document } from "@/lib/documents/types";
import { Card } from "@/components/ui/card";
import { DocumentUploadForm } from "@/components/portal/document-upload-form";
import { DownloadButton } from "@/components/portal/download-button";

async function getMyDocuments(): Promise<Document[]> {
  const res = await apiFetch("/api/documents");
  if (!res.ok) return [];
  return res.json();
}

const statusLabel: Record<Document["status"], string> = {
  pending: "Pending review",
  reviewed: "Reviewed",
  needs_info: "Needs more info",
};

export default async function PortalDashboardPage() {
  const documents = await getMyDocuments();

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Your personal zone</h1>
        <p className="mt-2 text-slate-600">Upload documents for Ilana to review.</p>
      </div>

      <Card className="max-w-md">
        <h2 className="mb-4 text-lg font-medium text-slate-900">Upload a document</h2>
        <DocumentUploadForm />
      </Card>

      <div>
        <h2 className="mb-3 text-lg font-medium text-slate-900">Your documents</h2>
        {documents.length === 0 ? (
          <Card>
            <p className="text-sm text-slate-500">No documents yet.</p>
          </Card>
        ) : (
          <div className="divide-y divide-slate-200 rounded-lg border border-slate-200 bg-white">
            {documents.map((doc) => (
              <div key={doc.id} className="flex items-center justify-between gap-4 px-4 py-3">
                <div>
                  <p className="text-sm font-medium text-slate-900">{doc.fileName}</p>
                  <p className="text-xs text-slate-500">
                    {doc.category ? `${doc.category} · ` : ""}
                    {statusLabel[doc.status]}
                  </p>
                </div>
                <DownloadButton documentId={doc.id} />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

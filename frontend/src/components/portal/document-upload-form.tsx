"use client";

import { useActionState } from "react";
import { uploadDocument, type UploadDocumentState } from "@/lib/documents/actions";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export function DocumentUploadForm() {
  const [state, formAction, pending] = useActionState<UploadDocumentState, FormData>(
    uploadDocument,
    undefined
  );

  return (
    <form action={formAction} className="space-y-4">
      <div>
        <label htmlFor="file" className="mb-1 block text-sm font-medium text-slate-700">
          File
        </label>
        <Input id="file" name="file" type="file" required />
      </div>
      <div>
        <label htmlFor="category" className="mb-1 block text-sm font-medium text-slate-700">
          Category (optional)
        </label>
        <Input id="category" name="category" placeholder="e.g. tax return, invoice" />
      </div>
      {state && "error" in state && <p className="text-sm text-red-600">{state.error}</p>}
      {state && "success" in state && (
        <p className="text-sm text-green-700">Uploaded successfully.</p>
      )}
      <Button type="submit" disabled={pending}>
        {pending ? "Uploading…" : "Upload document"}
      </Button>
    </form>
  );
}

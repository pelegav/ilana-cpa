"use client";

import { useState, useTransition } from "react";
import { getDocumentDownloadUrl } from "@/lib/documents/actions";
import { Button } from "@/components/ui/button";

export function DownloadButton({ documentId }: { documentId: string }) {
  const [isPending, startTransition] = useTransition();
  const [error, setError] = useState(false);

  return (
    <div className="flex items-center gap-2">
      <Button
        type="button"
        variant="secondary"
        disabled={isPending}
        onClick={() => {
          setError(false);
          startTransition(async () => {
            const url = await getDocumentDownloadUrl(documentId);
            if (url) {
              window.open(url, "_blank", "noopener,noreferrer");
            } else {
              setError(true);
            }
          });
        }}
      >
        {isPending ? "Preparing…" : "Download"}
      </Button>
      {error && <span className="text-xs text-red-600">Couldn&apos;t get download link.</span>}
    </div>
  );
}

"use server";

import { revalidatePath } from "next/cache";
import { apiFetch } from "@/lib/api/client";

export type UploadDocumentState = { error: string } | { success: true } | undefined;

export async function uploadDocument(
  _state: UploadDocumentState,
  formData: FormData
): Promise<UploadDocumentState> {
  const file = formData.get("file");
  const category = String(formData.get("category") ?? "").trim();

  if (!(file instanceof File) || file.size === 0) {
    return { error: "Please choose a file to upload." };
  }

  const upstream = new FormData();
  upstream.set("file", file);
  if (category) upstream.set("category", category);

  const res = await apiFetch("/api/documents", { method: "POST", body: upstream });

  if (!res.ok) {
    if (res.status === 413) {
      return { error: "That file is too large." };
    }
    return { error: "Failed to upload the document. Please try again." };
  }

  revalidatePath("/portal");
  return { success: true };
}

export async function getDocumentDownloadUrl(documentId: string): Promise<string | null> {
  const res = await apiFetch(`/api/documents/${documentId}/download`);
  if (!res.ok) return null;
  const data = await res.json();
  return data.url as string;
}

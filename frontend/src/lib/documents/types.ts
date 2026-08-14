export type Document = {
  id: string;
  ownerId: string;
  storagePath: string;
  fileName: string;
  category: string | null;
  status: "pending" | "reviewed" | "needs_info";
  uploadedAt: string;
  updatedAt: string;
};

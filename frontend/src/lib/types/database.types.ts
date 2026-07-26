// Hand-written placeholder matching supabase/migrations/0001_init.sql.
// Once the Supabase project is live, regenerate with:
//   npx supabase gen types typescript --project-id <project-id> > src/lib/types/database.types.ts
// and this file will be overwritten with the generated (more complete) version.

export type Database = {
  public: {
    Tables: {
      profiles: {
        Row: {
          id: string;
          email: string;
          full_name: string | null;
          role: "client" | "admin";
          status: "active" | "invited" | "disabled";
          created_at: string;
          updated_at: string;
        };
        Insert: {
          id: string;
          email: string;
          full_name?: string | null;
          role?: "client" | "admin";
          status?: "active" | "invited" | "disabled";
          created_at?: string;
          updated_at?: string;
        };
        Update: {
          id?: string;
          email?: string;
          full_name?: string | null;
          role?: "client" | "admin";
          status?: "active" | "invited" | "disabled";
          created_at?: string;
          updated_at?: string;
        };
        Relationships: [];
      };
      documents: {
        Row: {
          id: string;
          owner_id: string;
          storage_path: string;
          file_name: string;
          category: string | null;
          status: "pending" | "reviewed" | "needs_info";
          uploaded_at: string;
          updated_at: string;
        };
        Insert: {
          id?: string;
          owner_id: string;
          storage_path: string;
          file_name: string;
          category?: string | null;
          status?: "pending" | "reviewed" | "needs_info";
          uploaded_at?: string;
          updated_at?: string;
        };
        Update: {
          id?: string;
          owner_id?: string;
          storage_path?: string;
          file_name?: string;
          category?: string | null;
          status?: "pending" | "reviewed" | "needs_info";
          uploaded_at?: string;
          updated_at?: string;
        };
        Relationships: [
          {
            foreignKeyName: "documents_owner_id_fkey";
            columns: ["owner_id"];
            isOneToOne: false;
            referencedRelation: "profiles";
            referencedColumns: ["id"];
          }
        ];
      };
      document_notes: {
        Row: {
          id: string;
          document_id: string;
          author_id: string;
          body: string;
          created_at: string;
        };
        Insert: {
          id?: string;
          document_id: string;
          author_id: string;
          body: string;
          created_at?: string;
        };
        Update: {
          id?: string;
          document_id?: string;
          author_id?: string;
          body?: string;
          created_at?: string;
        };
        Relationships: [
          {
            foreignKeyName: "document_notes_document_id_fkey";
            columns: ["document_id"];
            isOneToOne: false;
            referencedRelation: "documents";
            referencedColumns: ["id"];
          },
          {
            foreignKeyName: "document_notes_author_id_fkey";
            columns: ["author_id"];
            isOneToOne: false;
            referencedRelation: "profiles";
            referencedColumns: ["id"];
          }
        ];
      };
    };
    Views: Record<string, never>;
    Functions: {
      is_admin: {
        Args: Record<string, never>;
        Returns: boolean;
      };
    };
    Enums: Record<string, never>;
    CompositeTypes: Record<string, never>;
  };
};

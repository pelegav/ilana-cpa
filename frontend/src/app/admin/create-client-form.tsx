"use client";

import { useActionState } from "react";
import { createClientAccount, type CreateClientState } from "./actions";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export function CreateClientForm() {
  const [state, formAction, pending] = useActionState<CreateClientState, FormData>(
    createClientAccount,
    undefined
  );

  return (
    <form action={formAction} className="space-y-4">
      <div>
        <label htmlFor="full_name" className="mb-1 block text-sm font-medium text-slate-700">
          Client name
        </label>
        <Input id="full_name" name="full_name" />
      </div>
      <div>
        <label htmlFor="email" className="mb-1 block text-sm font-medium text-slate-700">
          Client email
        </label>
        <Input id="email" name="email" type="email" required />
      </div>
      {state && "error" in state && <p className="text-sm text-red-600">{state.error}</p>}
      {state && "success" in state && (
        <div className="rounded-md bg-green-50 p-3 text-sm text-green-800">
          <p>
            Account created for <strong>{state.email}</strong>.
          </p>
          <p className="mt-1">
            Temporary password: <code className="font-mono">{state.temporaryPassword}</code>
          </p>
          <p className="mt-1 text-green-700">
            Share this with the client securely — it is only shown once.
          </p>
        </div>
      )}
      <Button type="submit" disabled={pending}>
        {pending ? "Creating…" : "Create client account"}
      </Button>
    </form>
  );
}

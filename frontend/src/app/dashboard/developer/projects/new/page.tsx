"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { developerService } from "@/services/developer-service";
import { oauthClientService } from "@/services/oauth-client-service";
import { ArrowLeft, AlertCircle } from "lucide-react";
import type { OAuthClient } from "@/types/api";

export default function NewProjectPage() {
  const router = useRouter();
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [oauthClientId, setOauthClientId] = useState("");
  const [oauthClients, setOauthClients] = useState<OAuthClient[]>([]);
  const [loadingClients, setLoadingClients] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useState(() => {
    oauthClientService
      .list()
      .then((data) => setOauthClients(data || []))
      .catch(() => {})
      .finally(() => setLoadingClients(false));
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    setSubmitting(true);
    setError(null);
    try {
      const project = await developerService.createProject({
        name: name.trim(),
        description: description.trim() || undefined,
        oauthClientId: oauthClientId || undefined,
      });
      router.push(`/dashboard/developer/projects/${project.id}`);
    } catch (err: unknown) {
      if (err && typeof err === "object" && "response" in err) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        setError(axiosErr.response?.data?.message || "Failed to create project");
      } else {
        setError("An unexpected error occurred");
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <Link
        href="/dashboard/developer/projects"
        className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to Projects
      </Link>

      <div>
        <h2 className="text-2xl font-bold tracking-tight">New Project</h2>
        <p className="text-muted-foreground">
          Create a new developer project
        </p>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <Card>
        <CardContent className="p-6">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <label htmlFor="name" className="text-sm font-medium leading-none">
                Project Name
              </label>
              <Input
                id="name"
                placeholder="My API Project"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="description" className="text-sm font-medium leading-none">
                Description
              </label>
              <Input
                id="description"
                placeholder="Optional project description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="oauthClient" className="text-sm font-medium leading-none">
                OAuth Client (optional)
              </label>
              <select
                id="oauthClient"
                value={oauthClientId}
                onChange={(e) => setOauthClientId(e.target.value)}
                className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"
                disabled={loadingClients}
              >
                <option value="">No OAuth client</option>
                {oauthClients.map((client) => (
                  <option key={client.id} value={client.id}>
                    {client.clientName} ({client.clientId})
                  </option>
                ))}
              </select>
            </div>

            <div className="flex gap-3 pt-2">
              <Button type="submit" disabled={submitting}>
                {submitting ? "Creating..." : "Create Project"}
              </Button>
              <Link href="/dashboard/developer/projects">
                <Button type="button" variant="outline">
                  Cancel
                </Button>
              </Link>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

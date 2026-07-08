"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Spinner } from "@/components/ui/spinner";
import { oauthClientService } from "@/services/oauth-client-service";
import {
  ArrowLeft,
  AlertCircle,
  CheckCircle2,
  Copy,
  Key,
  Eye,
  EyeOff,
} from "lucide-react";
import type { OAuthClientCreateResponse } from "@/types/api";

export default function NewOAuthClientPage() {
  const router = useRouter();
  const [clientId, setClientId] = useState("");
  const [clientName, setClientName] = useState("");
  const [confidential, setConfidential] = useState(false);
  const [clientSecret, setClientSecret] = useState("");
  const [redirectUris, setRedirectUris] = useState<string[]>([""]);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [created, setCreated] = useState<OAuthClientCreateResponse | null>(null);
  const [showSecret, setShowSecret] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleAddUri = () => {
    setRedirectUris((prev) => [...prev, ""]);
  };

  const handleRemoveUri = (index: number) => {
    setRedirectUris((prev) => prev.filter((_, i) => i !== index));
  };

  const handleUriChange = (index: number, value: string) => {
    setRedirectUris((prev) => {
      const next = [...prev];
      next[index] = value;
      return next;
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!clientName.trim() || !clientId.trim()) return;
    if (redirectUris.filter((u) => u.trim()).length === 0) return;

    setSubmitting(true);
    setError(null);
    try {
      const result = await oauthClientService.create({
        clientId: clientId.trim(),
        clientName: clientName.trim(),
        confidential,
        clientSecret: confidential ? clientSecret.trim() || undefined : undefined,
        redirectUris: redirectUris.filter((u) => u.trim()),
      });
      setCreated(result);
    } catch (err: unknown) {
      if (err && typeof err === "object" && "response" in err) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        setError(axiosErr.response?.data?.message || "Failed to create OAuth client");
      } else {
        setError("An unexpected error occurred");
      }
    } finally {
      setSubmitting(false);
    }
  };

  const copyToClipboard = async (text: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {}
  };

  if (created) {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Client Created</h2>
          <p className="text-muted-foreground">
            Your OAuth client has been created successfully
          </p>
        </div>

        <Card className="border-green-500/50">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-green-600">
              <CheckCircle2 className="h-5 w-5" />
              Client Created
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-3">
              <Key className="h-4 w-4 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">{created.clientName}</p>
                <p className="text-xs text-muted-foreground">Client ID: {created.clientId}</p>
              </div>
            </div>

            {created.clientSecret && (
              <div className="rounded-lg border border-amber-500/50 bg-amber-500/5 p-4">
                <p className="text-sm font-medium text-amber-600 mb-2">
                  Client Secret
                </p>
                <p className="text-xs text-muted-foreground mb-2">
                  Save this secret now. You will not be able to see it again.
                </p>
                <div className="flex items-center gap-2">
                  <code className="flex-1 rounded border bg-background px-3 py-2 text-sm font-mono text-xs">
                    {showSecret
                      ? created.clientSecret
                      : "•".repeat(created.clientSecret.length)}
                  </code>
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={() => setShowSecret(!showSecret)}
                  >
                    {showSecret ? (
                      <EyeOff className="h-4 w-4" />
                    ) : (
                      <Eye className="h-4 w-4" />
                    )}
                  </Button>
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={() => copyToClipboard(created.clientSecret)}
                  >
                    {copied ? (
                      <CheckCircle2 className="h-4 w-4 text-green-500" />
                    ) : (
                      <Copy className="h-4 w-4" />
                    )}
                  </Button>
                </div>
              </div>
            )}

            <div className="flex gap-3 pt-2">
              <Link href={`/dashboard/oauth-clients/${created.id}`}>
                <Button variant="outline">View Client</Button>
              </Link>
              <Link href="/dashboard/oauth-clients">
                <Button variant="outline">Back to Clients</Button>
              </Link>
              <Button onClick={() => setCreated(null)}>Create Another</Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <Link
        href="/dashboard/oauth-clients"
        className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to OAuth Clients
      </Link>

      <div>
        <h2 className="text-2xl font-bold tracking-tight">New OAuth Client</h2>
        <p className="text-muted-foreground">
          Create a new OAuth client application
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
              <label
                htmlFor="clientName"
                className="text-sm font-medium leading-none"
              >
                Client Name
              </label>
              <Input
                id="clientName"
                placeholder="My Application"
                value={clientName}
                onChange={(e) => setClientName(e.target.value)}
                required
              />
            </div>

            <div className="space-y-2">
              <label
                htmlFor="clientId"
                className="text-sm font-medium leading-none"
              >
                Client ID
              </label>
              <Input
                id="clientId"
                placeholder="my-app"
                value={clientId}
                onChange={(e) => setClientId(e.target.value)}
                required
              />
            </div>

            <div className="flex items-center gap-2">
              <input
                type="checkbox"
                id="confidential"
                checked={confidential}
                onChange={(e) => setConfidential(e.target.checked)}
                className="h-4 w-4 rounded border-input"
              />
              <label
                htmlFor="confidential"
                className="text-sm font-medium leading-none"
              >
                Confidential client (requires client secret)
              </label>
            </div>

            {confidential && (
              <div className="space-y-2">
                <label
                  htmlFor="clientSecret"
                  className="text-sm font-medium leading-none"
                >
                  Client Secret
                </label>
                <Input
                  id="clientSecret"
                  type="password"
                  placeholder="Optional: leave blank to generate"
                  value={clientSecret}
                  onChange={(e) => setClientSecret(e.target.value)}
                />
              </div>
            )}

            <div className="space-y-2">
              <label className="text-sm font-medium leading-none">
                Redirect URIs
              </label>
              {redirectUris.map((uri, index) => (
                <div key={index} className="flex items-center gap-2">
                  <Input
                    placeholder="https://example.com/callback"
                    value={uri}
                    onChange={(e) => handleUriChange(index, e.target.value)}
                    className="flex-1"
                  />
                  {redirectUris.length > 1 && (
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={() => handleRemoveUri(index)}
                    >
                      Remove
                    </Button>
                  )}
                </div>
              ))}
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={handleAddUri}
              >
                Add URI
              </Button>
            </div>

            <div className="flex gap-3 pt-2">
              <Button type="submit" disabled={submitting}>
                {submitting ? (
                  <>
                    <Spinner size="sm" className="mr-2" />
                    Creating...
                  </>
                ) : (
                  "Create Client"
                )}
              </Button>
              <Link href="/dashboard/oauth-clients">
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

"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { oauthClientService } from "@/services/oauth-client-service";
import {
  Key,
  ShieldCheck,
  ShieldOff,
  Globe,
  Calendar,
  ArrowLeft,
  AlertCircle,
  Fingerprint,
  Lock,
  Unlock,
  Copy,
  CheckCircle2,
} from "lucide-react";
import type { OAuthClient } from "@/types/api";

export default function OAuthClientDetailPage() {
  const params = useParams();
  const id = params.id as string;
  const [client, setClient] = useState<OAuthClient | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    async function load() {
      try {
        setError(null);
        const data = await oauthClientService.get(id);
        setClient(data);
      } catch {
        setError("Failed to load OAuth client");
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [id]);

  const copyToClipboard = async (text: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {}
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-48" />
        <Card>
          <CardContent className="p-6">
            <div className="space-y-4">
              <Skeleton className="h-6 w-64" />
              <Skeleton className="h-4 w-48" />
              <Skeleton className="h-4 w-32" />
              <Skeleton className="h-4 w-56" />
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (error || !client) {
    return (
      <div className="space-y-6">
        <Link
          href="/dashboard/oauth-clients"
          className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to OAuth Clients
        </Link>
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error || "Client not found"}</AlertDescription>
        </Alert>
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
        <h2 className="text-2xl font-bold tracking-tight">{client.clientName}</h2>
        <p className="text-muted-foreground font-mono text-sm">{client.clientId}</p>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Client Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-3">
              <Key className="h-4 w-4 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">{client.clientName}</p>
                <p className="text-xs text-muted-foreground">Name</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Fingerprint className="h-4 w-4 text-muted-foreground" />
              <div className="flex items-center gap-2">
                <div>
                  <p className="text-sm font-medium font-mono text-xs">{client.clientId}</p>
                  <p className="text-xs text-muted-foreground">Client ID</p>
                </div>
                <button
                  onClick={() => copyToClipboard(client.clientId)}
                  className="rounded p-1 hover:bg-accent transition-colors"
                >
                  {copied ? (
                    <CheckCircle2 className="h-3.5 w-3.5 text-green-500" />
                  ) : (
                    <Copy className="h-3.5 w-3.5 text-muted-foreground" />
                  )}
                </button>
              </div>
            </div>
            <div className="flex items-center gap-3">
              {client.confidential ? (
                <Lock className="h-4 w-4 text-muted-foreground" />
              ) : (
                <Unlock className="h-4 w-4 text-muted-foreground" />
              )}
              <div>
                <p className="text-sm font-medium">
                  {client.confidential ? "Confidential" : "Public"}
                </p>
                <p className="text-xs text-muted-foreground">Client Type</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              {client.enabled ? (
                <ShieldCheck className="h-4 w-4 text-green-500" />
              ) : (
                <ShieldOff className="h-4 w-4 text-red-500" />
              )}
              <div>
                <p className="text-sm font-medium">
                  {client.enabled ? "Enabled" : "Disabled"}
                </p>
                <p className="text-xs text-muted-foreground">Status</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Calendar className="h-4 w-4 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">
                  {new Date(client.createdAt).toLocaleString()}
                </p>
                <p className="text-xs text-muted-foreground">Created</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Redirect URIs</CardTitle>
          </CardHeader>
          <CardContent>
            {client.redirectUris.length === 0 ? (
              <p className="text-sm text-muted-foreground">No redirect URIs configured</p>
            ) : (
              <div className="space-y-2">
                {client.redirectUris.map((uri, i) => (
                  <div key={i} className="flex items-center gap-2 rounded-lg border p-3">
                    <Globe className="h-4 w-4 text-muted-foreground shrink-0" />
                    <span className="text-sm font-mono text-xs break-all">{uri}</span>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <Card className="opacity-60">
        <CardHeader>
          <CardTitle className="text-lg">Scopes & Grant Types</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground italic">
            Scope and grant type configuration coming soon
          </p>
        </CardContent>
      </Card>
    </div>
  );
}

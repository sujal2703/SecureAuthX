"use client";

import { useEffect, useState, useCallback } from "react";
import Link from "next/link";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { oauthClientService } from "@/services/oauth-client-service";
import {
  Key,
  Plus,
  ShieldOff,
  ShieldCheck,
  Globe,
  Calendar,
  AlertCircle,
  Search,
  ExternalLink,
} from "lucide-react";
import type { OAuthClient } from "@/types/api";

export default function OAuthClientsPage() {
  const [clients, setClients] = useState<OAuthClient[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isAdmin, setIsAdmin] = useState(true);
  const [search, setSearch] = useState("");
  const [typeFilter, setTypeFilter] = useState<string>("all");
  const [statusFilter, setStatusFilter] = useState<string>("all");

  const loadClients = useCallback(async () => {
    try {
      setError(null);
      const data = await oauthClientService.list();
      setClients(data || []);
    } catch (err: unknown) {
      if (err && typeof err === "object") {
        const axiosErr = err as { response?: { status?: number } };
        if (axiosErr.response?.status === 403) {
          setIsAdmin(false);
          return;
        }
      }
      setError("Failed to load OAuth clients");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadClients();
  }, [loadClients]);

  const filtered = clients.filter((c) => {
    const matchesSearch =
      !search ||
      c.clientName.toLowerCase().includes(search.toLowerCase()) ||
      c.clientId.toLowerCase().includes(search.toLowerCase());
    const matchesType =
      typeFilter === "all" ||
      (typeFilter === "confidential" && c.confidential) ||
      (typeFilter === "public" && !c.confidential);
    const matchesStatus =
      statusFilter === "all" ||
      (statusFilter === "enabled" && c.enabled) ||
      (statusFilter === "disabled" && !c.enabled);
    return matchesSearch && matchesType && matchesStatus;
  });

  if (!isAdmin) {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">OAuth Clients</h2>
          <p className="text-muted-foreground">
            Manage your OAuth client applications
          </p>
        </div>
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <ShieldOff className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">Admin access required</p>
            <p className="text-sm text-muted-foreground text-center max-w-md">
              OAuth client management is only available to administrators. Contact your
              administrator to create or manage OAuth clients.
            </p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">OAuth Clients</h2>
          <p className="text-muted-foreground">
            Manage your OAuth client applications
          </p>
        </div>
        <Link href="/dashboard/oauth-clients/new">
          <Button>
            <Plus className="mr-2 h-4 w-4" />
            New Client
          </Button>
        </Link>
      </div>

      <div className="flex flex-wrap gap-3">
        <div className="relative flex-1 min-w-[200px]">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search clients..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9"
          />
        </div>
        <select
          value={typeFilter}
          onChange={(e) => setTypeFilter(e.target.value)}
          className="h-10 rounded-md border border-input bg-background px-3 text-sm"
        >
          <option value="all">All Types</option>
          <option value="confidential">Confidential</option>
          <option value="public">Public</option>
        </select>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="h-10 rounded-md border border-input bg-background px-3 text-sm"
        >
          <option value="all">All Status</option>
          <option value="enabled">Enabled</option>
          <option value="disabled">Disabled</option>
        </select>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {loading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <Card key={i}>
              <CardContent className="p-6">
                <div className="space-y-3">
                  <Skeleton className="h-5 w-48" />
                  <Skeleton className="h-4 w-32" />
                  <Skeleton className="h-4 w-64" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <Key className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">
              {search || typeFilter !== "all" || statusFilter !== "all"
                ? "No clients match your filters"
                : "No OAuth clients"}
            </p>
            <p className="text-sm text-muted-foreground">
              {search || typeFilter !== "all" || statusFilter !== "all"
                ? "Try adjusting your search or filters"
                : "Create your first OAuth client to get started"}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {filtered.map((client) => (
            <Link key={client.id} href={`/dashboard/oauth-clients/${client.id}`}>
              <Card className="hover:bg-accent/50 transition-colors cursor-pointer">
                <CardContent className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex items-start gap-4">
                      <div className="mt-1">
                        <Key className="h-5 w-5 text-muted-foreground" />
                      </div>
                      <div className="space-y-2">
                        <div className="flex items-center gap-2">
                          <p className="font-medium">{client.clientName}</p>
                          {client.enabled ? (
                            <span className="rounded-full bg-green-500/10 px-2 py-0.5 text-xs font-medium text-green-600 flex items-center gap-1">
                              <ShieldCheck className="h-3 w-3" />
                              Enabled
                            </span>
                          ) : (
                            <span className="rounded-full bg-red-500/10 px-2 py-0.5 text-xs font-medium text-red-600 flex items-center gap-1">
                              <ShieldOff className="h-3 w-3" />
                              Disabled
                            </span>
                          )}
                        </div>
                        <div className="flex flex-wrap gap-x-4 gap-y-1 text-sm text-muted-foreground">
                          <span className="font-mono text-xs">{client.clientId}</span>
                          <span className="flex items-center gap-1">
                            {client.confidential ? "Confidential" : "Public"}
                          </span>
                        </div>
                        <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
                          {client.redirectUris.length > 0 && (
                            <span className="flex items-center gap-1">
                              <Globe className="h-3 w-3" />
                              {client.redirectUris.length} redirect URI{client.redirectUris.length !== 1 ? "s" : ""}
                            </span>
                          )}
                          <span className="flex items-center gap-1">
                            <Calendar className="h-3 w-3" />
                            Created: {new Date(client.createdAt).toLocaleDateString()}
                          </span>
                        </div>
                      </div>
                    </div>
                    <ExternalLink className="h-4 w-4 text-muted-foreground" />
                  </div>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}

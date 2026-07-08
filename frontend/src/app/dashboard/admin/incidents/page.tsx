"use client";

import { useEffect, useState, useCallback } from "react";
import Link from "next/link";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { useToast } from "@/hooks/use-toast";
import { ToastContainer } from "@/components/ui/toast";
import { adminService } from "@/services/admin-service";
import {
  ShieldAlert,
  Search,
  AlertCircle,
  ShieldOff,
  CheckCircle2,
  XCircle,
  Calendar,
  ArrowRight,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import type { SecurityIncidentResponse } from "@/types/api";

export default function SecurityIncidentsPage() {
  const [incidents, setIncidents] = useState<SecurityIncidentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isAdmin, setIsAdmin] = useState(true);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [resolvedFilter, setResolvedFilter] = useState<string>("all");
  const [resolvingId, setResolvingId] = useState<string | null>(null);
  const { toasts, addToast, removeToast } = useToast();

  const loadIncidents = useCallback(async () => {
    try {
      setError(null);
      const params: { page: number; size: number; resolved?: boolean } = { page, size: 20 };
      if (resolvedFilter === "resolved") params.resolved = true;
      if (resolvedFilter === "unresolved") params.resolved = false;
      const result = await adminService.listIncidents(params);
      setIncidents(result.content || []);
      setTotalPages(result.totalPages);
    } catch (err: unknown) {
      if (err && typeof err === "object") {
        const axiosErr = err as { response?: { status?: number } };
        if (axiosErr.response?.status === 403) {
          setIsAdmin(false);
          return;
        }
      }
      setError("Failed to load security incidents");
    } finally {
      setLoading(false);
    }
  }, [page, resolvedFilter]);

  useEffect(() => {
    loadIncidents();
  }, [loadIncidents]);

  const handleResolve = async (id: string) => {
    setResolvingId(id);
    try {
      await adminService.resolveIncident(id, { resolved: true });
      setIncidents((prev) => prev.map((i) => (i.id === id ? { ...i, resolved: true } : i)));
      addToast({ title: "Incident resolved", variant: "success" });
    } catch {
      addToast({ title: "Failed to resolve incident", variant: "destructive" });
    } finally {
      setResolvingId(null);
    }
  };

  const filtered = search
    ? incidents.filter(
        (i) =>
          i.incidentType.toLowerCase().includes(search.toLowerCase()) ||
          i.description.toLowerCase().includes(search.toLowerCase()) ||
          i.severity.toLowerCase().includes(search.toLowerCase()),
      )
    : incidents;

  const severityColor = (severity: string) => {
    switch (severity.toUpperCase()) {
      case "CRITICAL": return "text-red-600 bg-red-500/10";
      case "HIGH": return "text-orange-600 bg-orange-500/10";
      case "MEDIUM": return "text-amber-600 bg-amber-500/10";
      case "LOW": return "text-yellow-600 bg-yellow-500/10";
      default: return "text-muted-foreground bg-muted";
    }
  };

  if (!isAdmin) {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Security Incidents</h2>
          <p className="text-muted-foreground">Monitor security events</p>
        </div>
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <ShieldOff className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">Admin access required</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Security Incidents</h2>
        <p className="text-muted-foreground">Monitor security events</p>
      </div>

      <div className="flex flex-wrap gap-3">
        <div className="relative flex-1 min-w-[200px]">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search incidents..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9"
          />
        </div>
        <select
          value={resolvedFilter}
          onChange={(e) => { setResolvedFilter(e.target.value); setPage(0); }}
          className="h-10 rounded-md border border-input bg-background px-3 text-sm"
        >
          <option value="all">All Status</option>
          <option value="unresolved">Unresolved</option>
          <option value="resolved">Resolved</option>
        </select>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-16 w-full" />
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <ShieldAlert className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">No incidents found</p>
            <p className="text-sm text-muted-foreground">
              {search || resolvedFilter !== "all" ? "Try adjusting your filters" : "No security incidents recorded"}
            </p>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="space-y-3">
            {filtered.map((incident) => (
              <Card key={incident.id}>
                <CardContent className="p-4">
                  <div className="flex items-start justify-between">
                    <div className="space-y-1 flex-1">
                      <div className="flex items-center gap-2">
                        <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${severityColor(incident.severity)}`}>
                          {incident.severity}
                        </span>
                        <span className="text-sm font-medium">{incident.incidentType}</span>
                        {incident.resolved ? (
                          <span className="rounded-full bg-green-500/10 px-2 py-0.5 text-xs font-medium text-green-600 flex items-center gap-1">
                            <CheckCircle2 className="h-3 w-3" />
                            Resolved
                          </span>
                        ) : (
                          <span className="rounded-full bg-red-500/10 px-2 py-0.5 text-xs font-medium text-red-600 flex items-center gap-1">
                            <XCircle className="h-3 w-3" />
                            Open
                          </span>
                        )}
                      </div>
                      <p className="text-sm text-muted-foreground line-clamp-2">{incident.description}</p>
                      <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          {new Date(incident.createdAt).toLocaleString()}
                        </span>
                        <span>IP: {incident.ipAddress}</span>
                      </div>
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      <Link href={`/dashboard/admin/incidents/${incident.id}`}>
                        <Button variant="ghost" size="icon">
                          <ArrowRight className="h-4 w-4" />
                        </Button>
                      </Link>
                      {!incident.resolved && (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleResolve(incident.id)}
                          disabled={resolvingId === incident.id}
                        >
                          {resolvingId === incident.id ? "..." : "Resolve"}
                        </Button>
                      )}
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-4">
              <Button variant="outline" size="sm" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>
                <ChevronLeft className="h-4 w-4 mr-1" /> Previous
              </Button>
              <span className="text-sm text-muted-foreground">Page {page + 1} of {totalPages}</span>
              <Button variant="outline" size="sm" onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}>
                Next <ChevronRight className="h-4 w-4 ml-1" />
              </Button>
            </div>
          )}
        </>
      )}

      <ToastContainer toasts={toasts} removeToast={removeToast} />
    </div>
  );
}

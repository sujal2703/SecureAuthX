"use client";

import { useEffect, useState, useCallback } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { useToast } from "@/hooks/use-toast";
import { ToastContainer } from "@/components/ui/toast";
import { adminService } from "@/services/admin-service";
import {
  ShieldAlert,
  AlertCircle,
  ShieldOff,
  CheckCircle2,
  XCircle,
  Calendar,
  Globe,
  ArrowLeft,
  User,
} from "lucide-react";
import type { SecurityIncidentResponse } from "@/types/api";

export default function IncidentDetailPage() {
  const params = useParams();
  const id = params.id as string;
  const [incident, setIncident] = useState<SecurityIncidentResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isAdmin, setIsAdmin] = useState(true);
  const [resolving, setResolving] = useState(false);
  const { toasts, addToast, removeToast } = useToast();

  const loadIncident = useCallback(async () => {
    try {
      setError(null);
      const result = await adminService.getIncident(id);
      setIncident(result);
    } catch (err: unknown) {
      if (err && typeof err === "object") {
        const axiosErr = err as { response?: { status?: number } };
        if (axiosErr.response?.status === 403) {
          setIsAdmin(false);
          return;
        }
      }
      setError("Failed to load incident details");
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadIncident();
  }, [loadIncident]);

  const handleResolve = async () => {
    setResolving(true);
    try {
      await adminService.resolveIncident(id, { resolved: true });
      setIncident((prev) => prev ? { ...prev, resolved: true } : prev);
      addToast({ title: "Incident resolved", variant: "success" });
    } catch {
      addToast({ title: "Failed to resolve incident", variant: "destructive" });
    } finally {
      setResolving(false);
    }
  };

  if (!isAdmin) {
    return (
      <div className="space-y-6">
        <Link href="/dashboard/admin/incidents" className="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground">
          <ArrowLeft className="h-4 w-4" /> Back to Incidents
        </Link>
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <ShieldOff className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">Admin access required</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  const severityColor = (severity: string) => {
    switch (severity.toUpperCase()) {
      case "CRITICAL": return "text-red-600 bg-red-500/10";
      case "HIGH": return "text-orange-600 bg-orange-500/10";
      case "MEDIUM": return "text-amber-600 bg-amber-500/10";
      case "LOW": return "text-yellow-600 bg-yellow-500/10";
      default: return "text-muted-foreground bg-muted";
    }
  };

  return (
    <div className="space-y-6">
      <Link href="/dashboard/admin/incidents" className="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4" /> Back to Incidents
      </Link>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {loading ? (
        <div className="space-y-4">
          <Skeleton className="h-8 w-48" />
          <Skeleton className="h-32 w-full" />
          <Skeleton className="h-24 w-full" />
        </div>
      ) : incident ? (
        <>
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold tracking-tight">{incident.incidentType}</h2>
              <p className="text-muted-foreground">Incident Details</p>
            </div>
            {!incident.resolved && (
              <Button onClick={handleResolve} disabled={resolving}>
                {resolving ? "Resolving..." : "Resolve Incident"}
              </Button>
            )}
          </div>

          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Overview</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-2">
                <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${severityColor(incident.severity)}`}>
                  {incident.severity}
                </span>
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
              <p className="text-sm">{incident.description}</p>
              <div className="flex flex-wrap gap-x-6 gap-y-2 text-sm">
                <div className="flex items-center gap-1 text-muted-foreground">
                  <Calendar className="h-4 w-4" />
                  {new Date(incident.createdAt).toLocaleString()}
                </div>
                <div className="flex items-center gap-1 text-muted-foreground">
                  <Globe className="h-4 w-4" />
                  {incident.ipAddress}
                </div>
                <div className="flex items-center gap-1 text-muted-foreground">
                  <User className="h-4 w-4" />
                  User: {incident.userId}
                </div>
              </div>
              {incident.resolved && (
                <div className="rounded-lg bg-muted p-3 text-sm">
                  <span className="font-medium">Resolved</span>
                  {incident.resolvedBy && <span> by {incident.resolvedBy}</span>}
                  {incident.resolvedAt && (
                    <span> at {new Date(incident.resolvedAt).toLocaleString()}</span>
                  )}
                </div>
              )}
            </CardContent>
          </Card>
        </>
      ) : (
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <ShieldAlert className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">Incident not found</p>
          </CardContent>
        </Card>
      )}

      <ToastContainer toasts={toasts} removeToast={removeToast} />
    </div>
  );
}

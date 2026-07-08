"use client";

import { useEffect, useState, useCallback } from "react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { useToast } from "@/hooks/use-toast";
import { ToastContainer } from "@/components/ui/toast";
import { sessionService } from "@/services/session-service";
import {
  Monitor,
  Smartphone,
  Globe,
  Clock,
  Calendar,
  LogOut,
  ShieldOff,
  AlertCircle,
  CheckCircle2,
} from "lucide-react";
import type { Session } from "@/types/api";

export default function SessionsPage() {
  const [sessions, setSessions] = useState<Session[]>([]);
  const [loading, setLoading] = useState(true);
  const [revokingId, setRevokingId] = useState<string | null>(null);
  const [revokingAll, setRevokingAll] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { toasts, addToast, removeToast } = useToast();

  const loadSessions = useCallback(async () => {
    try {
      setError(null);
      const data = await sessionService.list();
      setSessions(data || []);
    } catch {
      setError("Failed to load sessions");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSessions();
  }, [loadSessions]);

  const handleRevoke = async (sessionId: string) => {
    setRevokingId(sessionId);
    try {
      await sessionService.revoke(sessionId);
      setSessions((prev) => prev.filter((s) => s.id !== sessionId));
      addToast({ title: "Session revoked", variant: "success" });
    } catch {
      addToast({ title: "Failed to revoke session", variant: "destructive" });
    } finally {
      setRevokingId(null);
    }
  };

  const handleRevokeAll = async () => {
    setRevokingAll(true);
    try {
      await sessionService.revokeAll();
      setSessions([]);
      addToast({ title: "All other sessions revoked", variant: "success" });
    } catch {
      addToast({
        title: "Failed to revoke sessions",
        variant: "destructive",
      });
    } finally {
      setRevokingAll(false);
    }
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleString();
  };

  const getDeviceIcon = (os?: string) => {
    if (!os) return <Monitor className="h-5 w-5" />;
    const lower = os.toLowerCase();
    if (lower.includes("android") || lower.includes("ios") || lower.includes("iphone") || lower.includes("ipad")) {
      return <Smartphone className="h-5 w-5" />;
    }
    return <Monitor className="h-5 w-5" />;
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Sessions</h2>
          <p className="text-muted-foreground">
            Manage your active sessions across devices
          </p>
        </div>
        {sessions.length > 1 && (
          <Button
            variant="destructive"
            onClick={handleRevokeAll}
            disabled={revokingAll}
          >
            <ShieldOff className="mr-2 h-4 w-4" />
            {revokingAll ? "Revoking..." : "Revoke All"}
          </Button>
        )}
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
      ) : sessions.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <CheckCircle2 className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">No active sessions</p>
            <p className="text-sm text-muted-foreground">
              All sessions have been revoked
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {sessions.map((session) => (
            <Card key={session.id}>
              <CardContent className="p-6">
                <div className="flex items-start justify-between">
                  <div className="flex items-start gap-4">
                    <div className="mt-1">
                      {getDeviceIcon(session.operatingSystem)}
                    </div>
                    <div className="space-y-2">
                      <div className="flex items-center gap-2">
                        <p className="font-medium">
                          {session.deviceName || "Unknown Device"}
                        </p>
                        {session.current && (
                          <span className="rounded-full bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary">
                            Current
                          </span>
                        )}
                      </div>
                      <div className="flex flex-wrap gap-x-4 gap-y-1 text-sm text-muted-foreground">
                        {session.browser && (
                          <span className="flex items-center gap-1">
                            <Globe className="h-3.5 w-3.5" />
                            {session.browser}
                          </span>
                        )}
                        {session.operatingSystem && (
                          <span className="flex items-center gap-1">
                            <Monitor className="h-3.5 w-3.5" />
                            {session.operatingSystem}
                          </span>
                        )}
                        {session.ipAddress && (
                          <span className="font-mono text-xs">
                            {session.ipAddress}
                          </span>
                        )}
                      </div>
                      <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          Created: {formatDate(session.createdAt)}
                        </span>
                        <span className="flex items-center gap-1">
                          <Clock className="h-3 w-3" />
                          Last activity: {formatDate(session.lastActivityAt)}
                        </span>
                      </div>
                    </div>
                  </div>
                  {!session.current && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleRevoke(session.id)}
                      disabled={revokingId === session.id}
                    >
                      <LogOut className="mr-2 h-4 w-4" />
                      {revokingId === session.id ? "Revoking..." : "Revoke"}
                    </Button>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <ToastContainer toasts={toasts} removeToast={removeToast} />
    </div>
  );
}

"use client";

import { useEffect, useState, useCallback } from "react";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { passkeyService } from "@/services/passkey-service";
import { sessionService } from "@/services/session-service";
import {
  Fingerprint,
  ShieldCheck,
  ShieldAlert,
  Clock,
  Monitor,
  AlertTriangle,
  CheckCircle2,
  Plus,
  ArrowRight,
  Key,
  Smartphone,
  AlertCircle,
} from "lucide-react";
import type { Passkey, Session } from "@/types/api";

export default function SecurityPage() {
  const [passkeys, setPasskeys] = useState<Passkey[]>([]);
  const [sessions, setSessions] = useState<Session[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    try {
      setError(null);
      const [passkeyData, sessionData] = await Promise.all([
        passkeyService.list(),
        sessionService.list(),
      ]);
      setPasskeys(passkeyData || []);
      setSessions(sessionData || []);
    } catch {
      setError("Failed to load security data");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const activeSessions = sessions.filter((s) => !s.revoked && !s.expired);
  const hasPasskeys = passkeys.length > 0;
  const recommendations: string[] = [];
  if (!hasPasskeys) {
    recommendations.push("Register a passkey for passwordless authentication");
  }
  if (activeSessions.length > 5) {
    recommendations.push("You have many active sessions. Consider revoking unused ones.");
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Security</h2>
          <p className="text-muted-foreground">
            Review your account security status
          </p>
        </div>
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3].map((i) => (
            <Card key={i}>
              <CardContent className="p-6">
                <Skeleton className="h-8 w-16 mb-2" />
                <Skeleton className="h-4 w-24" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Security</h2>
        <p className="text-muted-foreground">
          Review and improve your account security
        </p>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-primary/10 p-3">
              <Fingerprint className="h-6 w-6 text-primary" />
            </div>
            <div>
              <p className="text-2xl font-bold">{passkeys.length}</p>
              <p className="text-sm text-muted-foreground">Total Passkeys</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-primary/10 p-3">
              <ShieldCheck className="h-6 w-6 text-primary" />
            </div>
            <div>
              <p className="text-2xl font-bold">
                {hasPasskeys ? "Active" : "Inactive"}
              </p>
              <p className="text-sm text-muted-foreground">Password Status</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-primary/10 p-3">
              <Clock className="h-6 w-6 text-primary" />
            </div>
            <div>
              <p className="text-2xl font-bold">{activeSessions.length}</p>
              <p className="text-sm text-muted-foreground">Active Sessions</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div
              className={`rounded-lg p-3 ${
                hasPasskeys ? "bg-green-500/10" : "bg-amber-500/10"
              }`}
            >
              {hasPasskeys ? (
                <ShieldCheck className="h-6 w-6 text-green-500" />
              ) : (
                <ShieldAlert className="h-6 w-6 text-amber-500" />
              )}
            </div>
            <div>
              <p className="text-sm font-medium">
                {hasPasskeys ? "Good" : "Needs Attention"}
              </p>
              <p className="text-xs text-muted-foreground">Security Status</p>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <Fingerprint className="h-4 w-4" />
              Passkeys
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {hasPasskeys ? (
              <div className="space-y-2">
                {passkeys.slice(0, 3).map((pk) => (
                  <div
                    key={pk.id}
                    className="flex items-center gap-3 rounded-lg border p-3"
                  >
                    <Smartphone className="h-4 w-4 text-muted-foreground" />
                    <span className="text-sm">{pk.deviceName || "Unknown"}</span>
                  </div>
                ))}
              </div>
            ) : (
              <div className="flex flex-col items-center py-4">
                <Key className="h-8 w-8 text-muted-foreground" />
                <p className="mt-2 text-sm text-muted-foreground">
                  No passkeys registered
                </p>
              </div>
            )}
            <Link href="/dashboard/passkeys">
              <Button variant="outline" size="sm" className="w-full">
                Manage Passkeys
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </Link>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <Monitor className="h-4 w-4" />
              Active Sessions
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <p className="text-sm text-muted-foreground">
              You have {activeSessions.length} active session
              {activeSessions.length !== 1 ? "s" : ""}
            </p>
            <Link href="/dashboard/sessions">
              <Button variant="outline" size="sm" className="w-full">
                Manage Sessions
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </Link>
          </CardContent>
        </Card>
      </div>

      {recommendations.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <AlertTriangle className="h-4 w-4 text-amber-500" />
              Security Recommendations
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {recommendations.map((rec, i) => (
              <div
                key={i}
                className="flex items-start gap-3 rounded-lg border border-amber-500/20 bg-amber-500/5 p-3"
              >
                <AlertTriangle className="h-4 w-4 text-amber-500 mt-0.5 shrink-0" />
                <p className="text-sm">{rec}</p>
              </div>
            ))}
          </CardContent>
        </Card>
      )}
    </div>
  );
}

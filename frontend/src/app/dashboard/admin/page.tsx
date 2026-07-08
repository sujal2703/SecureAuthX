"use client";

import { useEffect, useState, useCallback } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { adminService } from "@/services/admin-service";
import {
  Users,
  Building2,
  Key,
  Fingerprint,
  Monitor,
  ShieldAlert,
  Activity,
  AlertCircle,
  ShieldOff,
  Layers,
} from "lucide-react";
import type { DashboardResponse } from "@/types/api";

export default function AdminDashboardPage() {
  const [data, setData] = useState<DashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isAdmin, setIsAdmin] = useState(true);

  const loadData = useCallback(async () => {
    try {
      setError(null);
      const result = await adminService.getDashboard();
      setData(result);
    } catch (err: unknown) {
      if (err && typeof err === "object") {
        const axiosErr = err as { response?: { status?: number } };
        if (axiosErr.response?.status === 403) {
          setIsAdmin(false);
          return;
        }
      }
      setError("Failed to load admin dashboard");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  if (!isAdmin) {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Admin Dashboard</h2>
          <p className="text-muted-foreground">Platform administration and monitoring</p>
        </div>
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <ShieldOff className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">Admin access required</p>
            <p className="text-sm text-muted-foreground text-center max-w-md">
              The admin dashboard is only available to administrators.
            </p>
          </CardContent>
        </Card>
      </div>
    );
  }

  const stats = data
    ? [
        { label: "Total Users", value: data.totalUsers, icon: Users, color: "bg-blue-500/10 text-blue-500" },
        { label: "Active Sessions", value: data.activeSessions, icon: Monitor, color: "bg-green-500/10 text-green-500" },
        { label: "Organizations", value: data.totalOrganizations, icon: Building2, color: "bg-purple-500/10 text-purple-500" },
        { label: "OAuth Clients", value: data.totalOAuthClients, icon: Key, color: "bg-amber-500/10 text-amber-500" },
        { label: "Passkeys", value: data.totalPasskeys, icon: Fingerprint, color: "bg-indigo-500/10 text-indigo-500" },
        { label: "Developer Projects", value: data.developerProjects, icon: Layers, color: "bg-cyan-500/10 text-cyan-500" },
        { label: "Total Sessions", value: data.totalSessions, icon: Activity, color: "bg-orange-500/10 text-orange-500" },
        { label: "Security Incidents", value: data.securityIncidents, icon: ShieldAlert, color: "bg-red-500/10 text-red-500" },
      ]
    : [];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Admin Dashboard</h2>
        <p className="text-muted-foreground">Platform administration and monitoring</p>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {loading ? (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <Card key={i}>
              <CardContent className="p-6">
                <Skeleton className="h-8 w-16 mb-2" />
                <Skeleton className="h-4 w-24" />
              </CardContent>
            </Card>
          ))}
        </div>
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            {stats.map((stat) => {
              const Icon = stat.icon;
              return (
                <Card key={stat.label}>
                  <CardContent className="flex items-center gap-4 p-6">
                    <div className={`rounded-lg p-3 ${stat.color}`}>
                      <Icon className="h-6 w-6" />
                    </div>
                    <div>
                      <p className="text-2xl font-bold">{stat.value}</p>
                      <p className="text-sm text-muted-foreground">{stat.label}</p>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>

          <div className="grid gap-6 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Platform Health</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex items-center justify-between rounded-lg border p-3">
                  <span className="text-sm">Total Login Events</span>
                  <span className="text-lg font-bold">{data?.totalLoginEvents}</span>
                </div>
                <div className="flex items-center justify-between rounded-lg border p-3">
                  <span className="text-sm">Active Sessions / Total</span>
                  <span className="text-lg font-bold">
                    {data?.activeSessions} / {data?.totalSessions}
                  </span>
                </div>
                <div className="flex items-center justify-between rounded-lg border p-3">
                  <span className="text-sm">Security Incidents</span>
                  <span className="text-lg font-bold">{data?.securityIncidents}</span>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-lg">System Status</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex items-center gap-3 rounded-lg border p-3">
                  <div className="h-3 w-3 rounded-full bg-green-500" />
                  <span className="text-sm">Authentication Service</span>
                  <span className="ml-auto text-xs text-green-600">Operational</span>
                </div>
                <div className="flex items-center gap-3 rounded-lg border p-3">
                  <div className="h-3 w-3 rounded-full bg-green-500" />
                  <span className="text-sm">Database</span>
                  <span className="ml-auto text-xs text-green-600">Operational</span>
                </div>
                <div className="flex items-center gap-3 rounded-lg border p-3">
                  <div className="h-3 w-3 rounded-full bg-green-500" />
                  <span className="text-sm">Redis Cache</span>
                  <span className="ml-auto text-xs text-green-600">Operational</span>
                </div>
              </CardContent>
            </Card>
          </div>
        </>
      )}
    </div>
  );
}

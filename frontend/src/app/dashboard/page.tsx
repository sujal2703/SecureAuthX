"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/hooks/use-auth";
import { sessionService } from "@/services/session-service";
import { passkeyService } from "@/services/passkey-service";
import {
  User,
  Monitor,
  Smartphone,
  ShieldCheck,
  Activity,
  ArrowRight,
  CheckCircle2,
  AlertTriangle,
} from "lucide-react";
import type { Session, Passkey } from "@/types/api";

export default function DashboardPage() {
  const { user } = useAuth();
  const [sessions, setSessions] = useState<Session[]>([]);
  const [passkeys, setPasskeys] = useState<Passkey[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        let sess: Session[] = [];
        let keys: Passkey[] = [];
        try {
          sess = await sessionService.list();
        } catch {}
        try {
          keys = await passkeyService.list();
        } catch {}
        setSessions(sess || []);
        setPasskeys(keys || []);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  const stats = [
    {
      title: "Account Status",
      value: user?.email ? "Active" : "Pending",
      icon: CheckCircle2,
      color: "text-green-500",
    },
    {
      title: "Active Sessions",
      value: loading ? "-" : sessions.length.toString(),
      icon: Monitor,
    },
    {
      title: "Registered Devices",
      value: loading ? "-" : passkeys.length.toString(),
      icon: Smartphone,
    },
    {
      title: "Security Status",
      value: passkeys.length > 0 ? "MFA Active" : "MFA Not Set Up",
      icon: ShieldCheck,
      color: passkeys.length > 0 ? "text-green-500" : "text-muted-foreground",
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Welcome{user?.firstName ? `, ${user.firstName}` : ""}</h2>
        <p className="text-muted-foreground">
          Here is an overview of your account and security status
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <Card key={stat.title}>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">
                  {stat.title}
                </CardTitle>
                <Icon className={`h-4 w-4 ${stat.color || "text-muted-foreground"}`} />
              </CardHeader>
              <CardContent>
                {loading ? (
                  <Skeleton className="h-8 w-20" />
                ) : (
                  <div className="text-2xl font-bold">{stat.value}</div>
                )}
              </CardContent>
            </Card>
          );
        })}
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <Card className="col-span-2">
          <CardHeader>
            <CardTitle className="text-lg">User Information</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="flex items-center gap-3">
                <User className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">{user?.email || "No email"}</p>
                  <p className="text-xs text-muted-foreground">Email</p>
                </div>
              </div>
              {user?.firstName && (
                <div className="flex items-center gap-3">
                  <User className="h-4 w-4 text-muted-foreground" />
                  <div>
                    <p className="text-sm font-medium">
                      {[user.firstName, user.lastName].filter(Boolean).join(" ") || "Not set"}
                    </p>
                    <p className="text-xs text-muted-foreground">Name</p>
                  </div>
                </div>
              )}
              <div className="flex items-center gap-3">
                <Activity className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">{sessions.length} active session{sessions.length !== 1 ? "s" : ""}</p>
                  <p className="text-xs text-muted-foreground">Sessions</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Smartphone className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">{passkeys.length} registered device{passkeys.length !== 1 ? "s" : ""}</p>
                  <p className="text-xs text-muted-foreground">Devices</p>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Quick Actions</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            <Link
              href="/dashboard/profile"
              className="flex items-center justify-between rounded-lg border p-3 text-sm hover:bg-accent transition-colors"
            >
              <span className="flex items-center gap-2">
                <User className="h-4 w-4" />
                View Profile
              </span>
              <ArrowRight className="h-4 w-4" />
            </Link>
            <Link
              href="/dashboard/sessions"
              className="flex items-center justify-between rounded-lg border p-3 text-sm hover:bg-accent transition-colors"
            >
              <span className="flex items-center gap-2">
                <Monitor className="h-4 w-4" />
                Manage Sessions
              </span>
              <ArrowRight className="h-4 w-4" />
            </Link>
            <Link
              href="/dashboard/devices"
              className="flex items-center justify-between rounded-lg border p-3 text-sm hover:bg-accent transition-colors"
            >
              <span className="flex items-center gap-2">
                <Smartphone className="h-4 w-4" />
                Manage Devices
              </span>
              <ArrowRight className="h-4 w-4" />
            </Link>
            <div className="flex items-center justify-between rounded-lg border p-3 text-sm text-muted-foreground">
              <span className="flex items-center gap-2">
                <ShieldCheck className="h-4 w-4" />
                Security Status
              </span>
              {passkeys.length > 0 ? (
                <CheckCircle2 className="h-4 w-4 text-green-500" />
              ) : (
                <AlertTriangle className="h-4 w-4 text-amber-500" />
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

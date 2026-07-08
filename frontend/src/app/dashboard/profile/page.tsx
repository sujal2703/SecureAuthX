"use client";

import { useEffect, useState } from "react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/hooks/use-auth";
import { organizationService } from "@/services/organization-service";
import {
  Mail,
  User,
  Building2,
  Shield,
  Calendar,
  Fingerprint,
} from "lucide-react";
import type { Organization } from "@/types/api";

export default function ProfilePage() {
  const { user } = useAuth();
  const [org, setOrg] = useState<Organization | null>(null);
  const [loadingOrg, setLoadingOrg] = useState(true);

  useEffect(() => {
    async function loadOrg() {
      try {
        const orgData = await organizationService.getCurrent();
        setOrg(orgData || null);
      } catch {
        setOrg(null);
      } finally {
        setLoadingOrg(false);
      }
    }
    loadOrg();
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Profile</h2>
        <p className="text-muted-foreground">
          Your account information and settings
        </p>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Account Details</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-3">
              <Mail className="h-4 w-4 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">{user?.email || "—"}</p>
                <p className="text-xs text-muted-foreground">Email</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <User className="h-4 w-4 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">
                  {[user?.firstName, user?.lastName].filter(Boolean).join(" ") || "—"}
                </p>
                <p className="text-xs text-muted-foreground">Name</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Shield className="h-4 w-4 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">{user?.roles?.length ? user.roles.join(", ") : "User"}</p>
                <p className="text-xs text-muted-foreground">Role</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Fingerprint className="h-4 w-4 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium font-mono text-xs">
                  {user?.id || "—"}
                </p>
                <p className="text-xs text-muted-foreground">User ID</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Organization</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {loadingOrg ? (
              <div className="space-y-3">
                <Skeleton className="h-4 w-40" />
                <Skeleton className="h-4 w-32" />
              </div>
            ) : org ? (
              <>
                <div className="flex items-center gap-3">
                  <Building2 className="h-4 w-4 text-muted-foreground" />
                  <div>
                    <p className="text-sm font-medium">{org.name}</p>
                    <p className="text-xs text-muted-foreground">Name</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <Shield className="h-4 w-4 text-muted-foreground" />
                  <div>
                    <p className="text-sm font-medium">{org.role}</p>
                    <p className="text-xs text-muted-foreground">Role</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <Calendar className="h-4 w-4 text-muted-foreground" />
                  <div>
                    <p className="text-sm font-medium">
                      {new Date(org.createdAt).toLocaleDateString()}
                    </p>
                    <p className="text-xs text-muted-foreground">Created</p>
                  </div>
                </div>
              </>
            ) : (
              <p className="text-sm text-muted-foreground">
                No organization found
              </p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

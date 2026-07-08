"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Users, ShieldOff } from "lucide-react";

export default function AdminUsersPage() {
  const isAdmin = true;

  if (!isAdmin) {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">User Management</h2>
          <p className="text-muted-foreground">Manage platform users</p>
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
        <h2 className="text-2xl font-bold tracking-tight">User Management</h2>
        <p className="text-muted-foreground">Manage platform users (coming soon)</p>
      </div>

      <Card>
        <CardContent className="flex flex-col items-center py-12">
          <Users className="h-12 w-12 text-muted-foreground" />
          <p className="mt-4 text-lg font-medium">User Management</p>
          <p className="text-sm text-muted-foreground text-center max-w-md mt-2">
            A dedicated user management endpoint is not yet available from the backend.
            User data can be viewed through the{" "}
            <a href="/dashboard/admin/audit-logs" className="text-primary underline underline-offset-2">
              Audit Logs
            </a>{" "}
            page, which tracks all user actions on the platform.
          </p>
          <div className="mt-6 grid gap-3 w-full max-w-md">
            <Card>
              <CardHeader>
                <CardTitle className="text-sm">Audit Trail</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                Review user activities including logins, registrations, and administrative actions in the audit logs.
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-sm">Role Management</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                Role and permission management will be available when the backend user management API is implemented.
              </CardContent>
            </Card>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

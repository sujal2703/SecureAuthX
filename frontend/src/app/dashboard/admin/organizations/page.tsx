"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Building2, ShieldOff } from "lucide-react";

export default function AdminOrganizationsPage() {
  const isAdmin = true;

  if (!isAdmin) {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Organization Admin</h2>
          <p className="text-muted-foreground">Manage organizations</p>
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
        <h2 className="text-2xl font-bold tracking-tight">Organization Admin</h2>
        <p className="text-muted-foreground">Manage organizations (coming soon)</p>
      </div>

      <Card>
        <CardContent className="flex flex-col items-center py-12">
          <Building2 className="h-12 w-12 text-muted-foreground" />
          <p className="mt-4 text-lg font-medium">Organization Administration</p>
          <p className="text-sm text-muted-foreground text-center max-w-md mt-2">
            Dedicated organization administration is not yet available from the backend.
            You can manage organizations through the{" "}
            <a href="/dashboard/organizations" className="text-primary underline underline-offset-2">
              Organizations
            </a>{" "}
            page.
          </p>
          <div className="mt-6 grid gap-3 w-full max-w-md">
            <Card>
              <CardHeader>
                <CardTitle className="text-sm">Organization Settings</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                Manage organization details, members, and roles from the Organizations section.
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-sm">Bulk Operations</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                Bulk organization management features will be available when the backend admin API is extended.
              </CardContent>
            </Card>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

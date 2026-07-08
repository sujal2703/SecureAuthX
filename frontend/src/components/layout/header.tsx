"use client";

import { usePathname } from "next/navigation";
import Link from "next/link";
import { ThemeToggle } from "@/components/layout/theme-toggle";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/hooks/use-auth";
import { Bell, User } from "lucide-react";

const pageTitles: Record<string, string> = {
  "/dashboard": "Dashboard",
  "/dashboard/profile": "Profile",
  "/dashboard/sessions": "Sessions",
  "/dashboard/devices": "Devices",
  "/dashboard/organizations": "Organizations",
  "/dashboard/oauth-clients": "OAuth Clients",
  "/dashboard/oauth-clients/new": "New OAuth Client",
  "/dashboard/developer": "Developer Portal",
  "/dashboard/developer/projects": "Projects",
  "/dashboard/developer/projects/new": "New Project",
  "/dashboard/passkeys": "Passkeys",
  "/dashboard/passkeys/register": "Register Passkey",
  "/dashboard/security": "Security",
  "/dashboard/settings": "Settings",
};

export function Header() {
  const pathname = usePathname();
  const { user } = useAuth();
  const title = pageTitles[pathname] || pageTitles[pathname.replace(/\/[^/]+$/, "")] || "Dashboard";

  return (
    <header className="flex h-16 items-center justify-between border-b bg-card px-6">
      <div>
        <h1 className="text-lg font-semibold">{title}</h1>
        <p className="text-sm text-muted-foreground">
          {user?.email ? `Welcome, ${user.email}` : "Welcome to SecureAuthX"}
        </p>
      </div>
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon">
          <Bell className="h-5 w-5" />
          <span className="sr-only">Notifications</span>
        </Button>
        <ThemeToggle />
        <Link href="/dashboard/profile">
          <Button variant="ghost" size="icon">
            <User className="h-5 w-5" />
            <span className="sr-only">Profile</span>
          </Button>
        </Link>
      </div>
    </header>
  );
}

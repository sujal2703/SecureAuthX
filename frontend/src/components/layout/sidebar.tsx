"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import {
  LayoutDashboard,
  User,
  Monitor,
  Smartphone,
  Settings,
  LogOut,
  ShieldCheck,
  Building2,
  Key,
  Code2,
  Fingerprint,
  Shield,
  ShieldHalf,
  UsersRound,
  ScrollText,
  ShieldAlert,
  Megaphone,
  Wrench,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/hooks/use-auth";

const navItems = [
  { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/dashboard/profile", label: "Profile", icon: User },
  { href: "/dashboard/organizations", label: "Organizations", icon: Building2 },
  { href: "/dashboard/oauth-clients", label: "OAuth Clients", icon: Key },
  { href: "/dashboard/developer", label: "Developer Portal", icon: Code2 },
  { href: "/dashboard/passkeys", label: "Passkeys", icon: Fingerprint },
  { href: "/dashboard/security", label: "Security", icon: Shield },
  { href: "/dashboard/sessions", label: "Sessions", icon: Monitor },
  { href: "/dashboard/devices", label: "Devices", icon: Smartphone },
  { href: "/dashboard/settings", label: "Settings", icon: Settings },
];

const adminNavItems = [
  { href: "/dashboard/admin", label: "Admin Dashboard", icon: ShieldHalf },
  { href: "/dashboard/admin/users", label: "Users", icon: UsersRound },
  { href: "/dashboard/admin/organizations", label: "Organization Admin", icon: Building2 },
  { href: "/dashboard/admin/audit-logs", label: "Audit Logs", icon: ScrollText },
  { href: "/dashboard/admin/incidents", label: "Security Incidents", icon: ShieldAlert },
  { href: "/dashboard/admin/announcements", label: "Announcements", icon: Megaphone },
  { href: "/dashboard/admin/settings", label: "System Settings", icon: Wrench },
];

function renderNavItem(item: { href: string; label: string; icon: React.ComponentType<{ className?: string }> }, pathname: string) {
  const Icon = item.icon;
  const isActive = pathname === item.href;
  return (
    <Link
      key={item.href}
      href={item.href}
      className={cn(
        "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
        isActive
          ? "bg-primary text-primary-foreground"
          : "text-muted-foreground hover:bg-accent hover:text-accent-foreground",
      )}
    >
      <Icon className="h-4 w-4" />
      {item.label}
    </Link>
  );
}

export function Sidebar() {
  const pathname = usePathname();
  const { logout } = useAuth();

  return (
    <aside className="flex h-full w-64 flex-col border-r bg-card">
      <div className="flex items-center gap-2 border-b px-6 py-4">
        <ShieldCheck className="h-6 w-6 text-primary" />
        <span className="text-lg font-semibold">SecureAuthX</span>
      </div>
      <nav className="flex-1 space-y-1 overflow-y-auto p-4">
        {navItems.map((item) => renderNavItem(item, pathname))}
        <div className="my-3 border-t" />
        <p className="px-3 text-xs font-medium text-muted-foreground uppercase tracking-wider">Admin</p>
        {adminNavItems.map((item) => renderNavItem(item, pathname))}
      </nav>
      <div className="border-t p-4">
        <Button
          variant="ghost"
          className="w-full justify-start gap-3 text-muted-foreground"
          onClick={logout}
        >
          <LogOut className="h-4 w-4" />
          Sign Out
        </Button>
      </div>
    </aside>
  );
}

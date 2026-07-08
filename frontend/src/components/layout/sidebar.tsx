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
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/hooks/use-auth";

const navItems = [
  { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/dashboard/profile", label: "Profile", icon: User },
  { href: "/dashboard/sessions", label: "Sessions", icon: Monitor },
  { href: "/dashboard/devices", label: "Devices", icon: Smartphone },
  { href: "/dashboard/settings", label: "Settings", icon: Settings },
];

export function Sidebar() {
  const pathname = usePathname();
  const { logout } = useAuth();

  return (
    <aside className="flex h-full w-64 flex-col border-r bg-card">
      <div className="flex items-center gap-2 border-b px-6 py-4">
        <ShieldCheck className="h-6 w-6 text-primary" />
        <span className="text-lg font-semibold">SecureAuthX</span>
      </div>
      <nav className="flex-1 space-y-1 p-4">
        {navItems.map((item) => {
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
        })}
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
